package com.owlplatform.example.solver;

import com.owlplatform.common.SampleMessage;
import com.owlplatform.solver.SolverAggregatorInterface;
import com.owlplatform.solver.listeners.ConnectionListener;
import com.owlplatform.solver.listeners.SampleListener;
import com.owlplatform.solver.protocol.messages.SubscriptionMessage;

/**
 * A simple example of using the Owl Platform Solver library by printing
 * received samples to standard output (System.out). This is not an example of a
 * useful solver, which would produce some new type of information from various
 * data sources, but demonstrates how to interact with the aggregator.
 * 
 * @author Robert Moore
 */
public class AsyncFakeSolver implements SampleListener, ConnectionListener {

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
    SolverAggregatorInterface agg = new SolverAggregatorInterface();
    
    AsyncFakeSolver solver = new AsyncFakeSolver();
  }

  @Override
  public void connectionEnded(SolverAggregatorInterface aggregator) {
    // TODO Auto-generated method stub

  }

  @Override
  public void connectionEstablished(SolverAggregatorInterface aggregator) {
    // TODO Auto-generated method stub

  }

  @Override
  public void connectionInterrupted(SolverAggregatorInterface aggregator) {
    // TODO Auto-generated method stub

  }

  @Override
  public void subscriptionReceived(SolverAggregatorInterface aggregator,
      SubscriptionMessage response) {
    // TODO Auto-generated method stub

  }

  @Override
  public void sampleReceived(SolverAggregatorInterface aggregator,
      SampleMessage sample) {
    // TODO Auto-generated method stub

  }

}
