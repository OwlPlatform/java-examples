#!/bin/bash

#Location of jar file 
JAR_FILE=target/java-examples-1.0.0-SNAPSHOT-jar-with-dependencies.jar
LAUNCH_CLASS=com.owlplatform.example.solver.SimpleFakeSolver

# Solver port for WM
PORT=7008

usage() {
  echo "Usage: `basename $0` [-h] [-p PORT] AGG_HOST \
{TX1 | -x HEX_TX1}*"
}

parseopts() {
  while getopts ":hp:" optname 
    do
      case "$optname" in
        "p")
          PORT="$OPTARG"
          ;;
        "h")
          usage
          exit 0
          ;;
        "?")
          echo "Unknown option $OPTARG"
          ;;
        ":")
          echo "Missing value for option $OPTARG"
          ;;
        *)
          echo "Unknown error has occurred"
          ;;
    esac
  done
  return $OPTIND
}

parseopts "$@"
argstart=$?
shift $(($argstart-1))

if [ $# -lt 1 ]
then
  usage
  exit 1
fi

AGG_HOST=$1
shift
java -cp $JAR_FILE $LAUNCH_CLASS $AGG_HOST $PORT $@
