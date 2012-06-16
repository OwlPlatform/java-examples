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
package com.owlplatform.example.sensor;

import java.util.Timer;
import java.util.TimerTask;

import com.owlplatform.common.SampleMessage;
import com.owlplatform.sensor.SensorAggregatorInterface;
import com.owlplatform.sensor.listeners.ConnectionListener;

/**
 * <p>A simple class demonstrating how to write a Java-based sensor component by
 * sending a single sample message once per second.</p>
 * 
 * <p>This class makes use of the {@link SensorAggregatorInterface} in the simplest
 * way possible - by polling its state and treating it strictly as a generic object.
 * However, the {@code SensorAggregatorInterface} supports the Observable
 * pattern, and registering as a {@link ConnectionListener} allows more
 * efficient interaction at the cost of greater complexity.</p>
 * 
 * @author Robert Moore
 * 
 */
public class SimpleFakeSensor {

  /**
   * Takes the command-line arguments (aggregator host, port), creates a new
   * {@code SensorAggregatorInterface} and sends a sample once per second.
   * 
   * @param args
   * @throws Exception
   *           if an exception is thrown by any of the code
   */
  public static void main(String[] args) throws Exception {
    if (args.length != 2) {
      System.err.println("Invalid number of arguments.");
      System.err.println("Usage: <Aggregator Host> <Aggregator Port>");
      return;
    }

    final SensorAggregatorInterface agg = new SensorAggregatorInterface();
    // Any exception should cause a disconnect
    agg.setDisconnectOnException(true);

    // Since this is an example, don't worry about reconnecting
    agg.setStayConnected(false);

    // Add a shutdown hook to gracefully disconnect from the aggregator
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        System.out.println("Disconnecting...");
        agg.disconnect();
        System.out.println("Disconnected.");
      }
    });

    // Set the host and port for the aggregator
    agg.setHost(args[0]);
    int port = Integer.parseInt(args[1]);
    agg.setPort(port);

    // Try to connect, timing-out after 10 seconds.
    if (!agg.connect(10000L)) {
      System.err.println("Unable to connect to aggregator at " + args[0] + ":"
          + port);
      return;
    }

    // Wait until the aggregator is ready to accept data
    // Wait up to 1 second (100*10ms)
    int attempts = 0;
    while (!agg.isCanSendSamples()) {
      ++attempts;
      try {
        Thread.sleep(10);
      } catch (InterruptedException ie) {
        // Ignored
      }
      if (attempts > 100) {
        System.err
            .println("Aggregator didn't become ready after 1 second. Giving-up!");
        return;
      }
    }

    // Ready to send samples!
    final Timer aTimer = new Timer();
    aTimer.schedule(new TimerTask() {

      private final byte[] DEV_ID = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
          11, 12, 13, 14, 15, 16 };
      private final byte[] RECV_ID = new byte[] { 16, 15, 14, 13, 12, 11, 10,
          9, 8, 7, 6, 5, 4, 3, 2, 1 };
      private final float RSSI = -50;
      private final byte PHY = 1;
      private final byte[] DATA = new byte[] { (byte) 0xAB, (byte) 0xBA };

      @Override
      public void run() {
        // Create a generic sample message and send it.
        SampleMessage msg = new SampleMessage();
        msg.setPhysicalLayer(this.PHY);
        msg.setRssi(this.RSSI);
        msg.setDeviceId(this.DEV_ID);
        msg.setReceiverId(this.RECV_ID);
        msg.setSensedData(this.DATA);
        if (!agg.sendSample(msg)) {
          // If the send fails for any reason, exit.
          System.err.println("Unable to send a sample!");
          aTimer.cancel();
        }else{
          System.out.println("Sent " + msg);
        }
        
      }
    }, 10, 1000);
  }

}
