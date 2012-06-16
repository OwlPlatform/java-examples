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
 * A simple class demonstrating how to interact with the
 * {@link SensorAggregatorInterface} class using the asynchronous
 * listener/Observable pattern it supports. This application will perform the
 * same actions as {@link SimpleFakeSensor}&mdash;sending a fake sample once per
 * second&mdash;but will do so using an asynchronous approach.
 * 
 * @author Robert Moore
 * 
 */
public class AsyncFakeSensor implements ConnectionListener {

  /**
   * Parses the command-line arguments (aggregator host, port), creates a new
   * {@code AsyncFakeSensor} object, registers it with the
   * {@code SensorAggregatorInterface}, and gets things started.
   * 
   * @param args
   *          an aggregator host and port number.
   * @throws Exception
   *           if an exception is thrown
   */
  public static void main(String[] args) throws Exception {
    if (args.length != 2) {
      System.err.println("Invalid number of arguments.");
      System.err.println("Usage: <Aggregator Host> <Aggregator Port>");
      return;
    }
    final SensorAggregatorInterface agg = new SensorAggregatorInterface();

    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        System.out.println("Disconnecting from aggregator...");
        agg.disconnect();
        System.out.println("Disconnected.");
      }
    });

    /*
     * No need to stay connected since this is an example, though in real life
     * this would be handy.
     */
    agg.setStayConnected(false);
    agg.setDisconnectOnException(true);

    agg.setHost(args[0]);
    int port = Integer.parseInt(args[1]);
    agg.setPort(port);

    AsyncFakeSensor sensor = new AsyncFakeSensor();
    agg.addConnectionListener(sensor);

    if (!agg.connect(10000L)) {
      System.err.println("Unable to connect to " + agg);
    }
  }

  /**
   * Timer for sending samples once per second.
   */
  final Timer timer = new Timer();

  @Override
  public void connectionEnded(SensorAggregatorInterface aggregator) {
    System.err.println("Lost connection to " + aggregator);
    System.exit(1);
  }

  @Override
  public void connectionEstablished(SensorAggregatorInterface aggregator) {
    System.out.println("Socket created. Connected to " + aggregator);
  }

  @Override
  public void connectionInterrupted(SensorAggregatorInterface aggregator) {
    System.out
        .println("Connection was temporarily lost. Library may try again.");
  }

  @Override
  public void readyForSamples(final SensorAggregatorInterface aggregator) {
    System.out.println("Handshakes exchanged. Sending samples.");
    this.timer.schedule(new TimerTask() {
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
        if (!aggregator.sendSample(msg)) {
          // If the send fails for any reason, exit.
          System.err.println("Unable to send a sample!");
          AsyncFakeSensor.this.timer.cancel();
        } else {
          System.out.println("Sent " + msg);
        }

      }
    }, 10, 1000);

  }

}
