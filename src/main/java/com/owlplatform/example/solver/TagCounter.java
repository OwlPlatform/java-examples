/*
 * Owl Platform Example Code
 * Copyright (C) 2012 Robert Moore and the Owl Platform
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *  
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.owlplatform.example.solver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.owlplatform.common.SampleMessage;
import com.owlplatform.common.util.HashableByteArray;
import com.owlplatform.common.util.NumericUtils;
import com.owlplatform.solver.SolverAggregatorConnection;
import com.owlplatform.solver.protocol.messages.Transmitter;
import com.owlplatform.solver.rules.SubscriptionRequestRule;

/**
 * A simple example of using the Owl Platform Solver library by printing
 * received samples to standard output (System.out). This is not an example of a
 * useful solver, which would produce some new type of information from various
 * data sources, but demonstrates how to interact with the aggregator.
 * 
 * @author Robert Moore
 */
public class TagCounter {

  /**
   * <p>
   * Expects two arguments: aggregator host and aggregator solver port.
   * Subscribes to all sample messages with an update interval of 1 second. This
   * means that it should only receive a sample for a receiver/transmitter pair
   * at MOST once per second. If a device transmits at once per second, it is
   * possible that some samples may be lost due to network delay and timing
   * issues.
   * <p>
   * <p>
   * Additional parameters specify specific transmitter ID values to filter from
   * the aggregator. ID values are 32-bit signed integers by default, and
   * hexadecimal values may be provided by leading with the "-x" switch. For
   * example:
   * 
   * <pre>
   * (java invocation) localhost 7008 1234 -x FFE 99
   * </pre>
   * 
   * Specifies connecting to the aggregator at localhost:7008 and requesting
   * only the transmitters with ID value "1234" (0x04D2), "0x0FFE" (4094), or
   * "99" (0x63).
   * </p>
   * 
   * @param args
   *          aggregator host, port, and a list of optional transmitter ID
   *          values
   */
  public static void main(String[] args) {
    if (args.length < 2) {
      System.err.println("Invalid number of arguments.");
      System.err.println("Usage: <Aggregator Host> <Aggregator Port>");
      return;
    }

    String host = args[0];
    int port = Integer.parseInt(args[1]);

    // Array of Transmitter filters
    ArrayList<Transmitter> txers = null;
    if (args.length > 2) {

      txers = new ArrayList<Transmitter>();
      for (int argc = 2; argc < args.length; ++argc) {
        String arg = args[argc];
        byte[] value = null;
        // Flag to indicate hex string, next arg is actual value.
        if ("-x".equalsIgnoreCase(arg)) {
          value = NumericUtils.fromHexString(args[++argc]);
        }
        // Parse an integer (32-bit, signed)
        else {
          value = new byte[4];
          int valInt = Integer.parseInt(arg);
          value[0] = (byte) (valInt >> 24);
          value[1] = (byte) (valInt >> 16);
          value[2] = (byte) (valInt >> 8);
          value[3] = (byte) valInt;
        }
        txers.add(new Transmitter(value));
      }

    }

    final SolverAggregatorConnection agg = new SolverAggregatorConnection();
    agg.setHost(host);
    agg.setPort(port);

    Runtime.getRuntime().addShutdownHook(new Thread() {

      @Override
      public void run() {
        agg.disconnect();
        System.out.println("Shutdown complete.");
      }
    });

    SubscriptionRequestRule rule = SubscriptionRequestRule
        .generateGenericRule();
    rule.setUpdateInterval(0l);
    if (txers != null) {
      rule.setTransmitters(txers);
    }

    agg.addRule(rule);

    if (!agg.connect(10000l)) {
      System.err.println("Unable to connect to " + agg);
      return;
    }

    // Wait up to 1 second for the subscription response
    int waitAttempts = 0;
    while (!agg.isSubscriptionAcknowledged()) {
      // Waited > 5 second
      if (waitAttempts > 50) {
        System.err
            .println("Aggregator never acknowledged the subscription request.");
        agg.disconnect();
        return;
      }
      // Got disconnected
      if (!agg.isConnected()) {
        System.err.println("Lost connection to the aggregator.");
        return;
      }
      try {
        System.out.println("Waiting for subscription response (100ms)");
        Thread.sleep(100);
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      ++waitAttempts;
    }

    HashMap<Integer, Boolean> ids = new HashMap<Integer, Boolean>();
    HashMap<Integer, Integer> recvs = new HashMap<Integer, Integer>();
    HashMap<Integer, Float> temps = new HashMap<Integer, Float>();
    HashMap<Integer, Boolean> onOffs = new HashMap<Integer, Boolean>();
    long lastPrint = System.currentTimeMillis();

    // Start printing samples.
    while (agg.isConnected() && agg.isSubscriptionAcknowledged()) {
      SampleMessage msg = agg.getNextSample();
      if (msg == null
          || msg.getPhysicalLayer() != SampleMessage.PHYSICAL_LAYER_PIPSQUEAK) {

        continue;
      }

      int id = (msg.getDeviceId()[15] & 0xFF)
          | ((msg.getDeviceId()[14] << 8) & 0xFF00)
          | ((msg.getDeviceId()[13] << 16) & 0xFF0000)
          | ((msg.getDeviceId()[12] << 24) & 0xFF000000);

      int rec = (msg.getReceiverId()[15] & 0xFF)
          | ((msg.getReceiverId()[14] << 8) & 0xFF00)
          | ((msg.getReceiverId()[13] << 16) & 0xFF0000)
          | ((msg.getReceiverId()[12] << 24) & 0xFF000000);
      if (ids.get(id) == null) {
        ids.put(id, Boolean.FALSE);
      } else {
        ids.put(id, Boolean.TRUE);
      }

      if (recvs.get(rec) == null) {
        recvs.put(rec, Integer.valueOf(1));
      } else {
        recvs.put(rec, Integer.valueOf(recvs.get(rec).intValue() + 1));
      }

      if (msg.getSensedData() != null && msg.getSensedData().length >= 2) {
        byte[] data = msg.getSensedData();
        float temp = -274;
        // 16-bit temperature
        if ((data[0] & 0x02) == 0x02 && data.length >= 3) {
          int startIdx = 1;
          if ((data[0] & 0x01) == 0x01) {
            startIdx = 2;
          }
          byte[] tBytes = new byte[2];
          System.arraycopy(data, startIdx, tBytes, 0, 2);
          temp = convTemp(tBytes);
        } else if ((data[0] & 0x01) == 0x01) {
          temp = ((data[1] >> 1) & 0x7F) - 40;
        }
        temps.put(id, Float.valueOf(temp));
        onOffs.put(id, (msg.getSensedData()[1] & 0x01) == 1 ? Boolean.TRUE
            : Boolean.FALSE);
      }

      if (System.currentTimeMillis() - lastPrint > 35000) {
        ArrayList<Integer> identNums = new ArrayList<Integer>(ids.size());
        ArrayList<Integer> recvNums = new ArrayList<Integer>(ids.size());

        for (Integer theId : ids.keySet()) {
          if (ids.get(theId).booleanValue()) {
            // System.out.println(id);

            identNums.add(theId);

          }
          ids.put(theId, Boolean.FALSE);
        }

        for (Integer r : recvs.keySet()) {
          if (recvs.get(r).intValue() > 1) {
            // System.out.println(id);

            recvNums.add(r);

          }

        }

        Collections.sort(identNums);
        Collections.sort(recvNums);

        System.out.println("########## RECEIVERS ##########");

        int c = 0;
        for (Integer i : recvNums) {
          System.out.printf("%4d: %,4d | ", i, recvs.get(i));
          c++;
          if (c >= 5) {
            System.out.println();
            c = 0;
          }
        }

        recvs.clear();

        System.out.printf(
            "\n======================================\nTotal: %d\n\n",
            recvNums.size());

        System.out.println("########## TRANSMITTERS ##########");

        c = 0;
        for (Integer i : identNums) {
          Float temp = temps.remove(i);
          Boolean on = onOffs.remove(i);
          System.out.printf("%4d: %5.2fC %s| ", i, temp == null ? -274 : temp,
              on == null ? " " : on.booleanValue() ? "#" : " ");
          c++;
          if (c >= 5) {
            System.out.println();
            c = 0;
          }
        }

        System.out.printf(
            "\n======================================\nTotal: %d\n\n",
            identNums.size());
        lastPrint = System.currentTimeMillis();

      }

      // System.out.println(msg);
    }
  }

  private static float convTemp(byte[] asBytes) {
    if (asBytes == null || asBytes.length != 2) {
      return -274;
    }
    int wholevalue = (asBytes[0] * 16 + ((asBytes[1] >> 4)&0x0F)) - 40;
    int sixteenths = asBytes[1] & 0x0f;
    return wholevalue+ (sixteenths / 16f);
  }

}
