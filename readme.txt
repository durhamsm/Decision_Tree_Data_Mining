Notes About Running program:

Navigate to "DecisionTree Source code - for running" directory and then run following command from prompt:

javac *.java

Execute code with following Command:

java Main

The result will be an "output.txt" file in that same directory that includes the following error/accuracy calculations (in this order):

Pessimistic Error
Training Error
Prediction Error
Minimum Description Length
Number of Nodes
precision
recall
false positive rate
true Positive rate
balanced sample accuracy
Simple Accuracy
Fmeasure

In order to change the various settings of the decision tree construction algorithm:


Running different data sets
Alter the file name in first line of “main” method “Main.java” in order to analyze various data sets
Within Record.java file, at the top of the Record class, you should uncomment the three lines for the particular data set that you want to analyze. Only one set of 3 lines should be uncommented


Using GINI or Information Gain for determining best feature split
Within ClassificationParameters.java file, you should change “featureSplitScore” member to appropriate initialization (see comments in code for more info)

Changing the error measure that is used for overfitting prevention
Within “ClassificationParameters.java” file, change “useTreeOverFittingPrevention” initialization value to “true”
Within “DecisionTree.java”, go to “getTreeScore” method, and uncomment the line of code for the error measure that you want to use

Altering the “m” value for solution to problem 6
Within “ClassificationParameters.java” file, change “m” initialization value to appropriate positive integer (in case of greedy solution for problem 4, m = 1)

Additionally, you may want to change the error method that is used to determine which decision trees are the “best” (see 3b above).
