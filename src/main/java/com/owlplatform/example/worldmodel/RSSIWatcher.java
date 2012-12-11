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
package com.owlplatform.example.worldmodel;

import java.util.Date;

import com.owlplatform.worldmodel.Attribute;
import com.owlplatform.worldmodel.client.ClientWorldConnection;
import com.owlplatform.worldmodel.client.StepResponse;
import com.owlplatform.worldmodel.client.WorldState;
import com.owlplatform.worldmodel.types.DataConverter;
import com.owlplatform.worldmodel.types.DoubleConverter;

/**
 * Connects to a world model server, and requests streaming updates for the
 * RSSI values of all transmitter/receiver pairs or for an optional Identifier
 * (or regular expression matching Identifiers). Prints out all updated
 * Attribute values in a simple way. Requires a solver to generate the
 * "link average" attribute.
 * 
 * @author Robert Moore
 */
public class RSSIWatcher {

  /**
   * Parse command-line arguments (host, client port, Identifier), request
   * updates, and print.
   * 
   * @param args
   *          world model server hostname/IP, client port, Identifier regular
   *          expression.
   */
  public static void main(String[] args) {

    // Verify number of arguments
    if (args.length < 2) {
      System.out
          .println("Invalid number of arguments.\nExpected: <WM Host> <WM Port> [Identifier]");
      return;
    }

    // Get the port number
    int port = -1;
    try {
      port = Integer.parseInt(args[1]);
    } catch (NumberFormatException nfe) {
      System.out.println(args[1] + " is not a valid port number.");
      return;
    }

    if (port < 0) {
      System.out.println("Invalid port number provided: " + port);
      return;
    }

    // Create the connector
    final ClientWorldConnection cwc = new ClientWorldConnection();
    cwc.setHost(args[0]);
    cwc.setPort(port);

    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        cwc.disconnect();
      }
    });

    // Try to connect, timeout after 10 seconds
    if (!cwc.connect(10000l)) {
      System.out.println("Unable to connect to " + cwc + " after 10 seconds.");
      return;
    }

    String identifier = ".*";

    if (args.length > 2) {
      identifier = args[2];
    }

    System.out.println("Requesting updates for \"" + identifier + "\".");

    // Send a streaming request, get back the response
    StepResponse resp = cwc.getStreamRequest(identifier,
        System.currentTimeMillis(), 0l, "link average");

    // Keep going until the response is complete, has an error, or an exception
    // occurs.
    while (!resp.isComplete() && !resp.isError()) {
      try {
        // Get the next set of Attribute updates
        WorldState state = resp.next();
        for (String id : state.getIdentifiers()) {
          for (Attribute att : state.getState(id)) {
            if (att.getAttributeName() == null) {
              System.out.println("Skipping unnamed attribute for " + id);
              continue;
            }
            System.out.printf(
                "[%s] %s/%s changed to %,.2f by %s\n",
                new Date(att.getCreationDate()),
                id,att.getAttributeName(),(att.getData() == null ? "" : DoubleConverter.get()
                    .decode(att.getData())),att.getOriginName());
          }

        }
      } catch (Exception e) {
        System.out.println("An exception has occurred: " + e.getMessage());
        e.printStackTrace(System.out);
        break;
      }
    }

    System.out.println("Exiting.");
    // Shut down the connection
    cwc.disconnect();
  }

}
