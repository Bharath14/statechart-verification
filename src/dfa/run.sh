JFLEXPATH=/home/bharath/Desktop/StateChart/statechart-verification/src/dfa/lib
MYCLASSPATH=/home/bharath/Desktop/StateChart/statechart-verification/src/dfa/lib/junit-4.8.1.jar:./:./classes/:${JFLEXPATH}/java-cup-11a.jar
if [ $# = 1 ]; then
  echo 1
  testcase=$1
fi
if [ $# = 0 ]; then
  echo 2
  testcase=TestAll
fi

java -Xss4m -cp ${MYCLASSPATH} org.junit.runner.JUnitCore testcases.${testcase}
