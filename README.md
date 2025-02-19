# JuiceBottler

Juice Bottler is an exercise in both Data and Task Parallelization
For Juice Bottler to work we must have multiple "Plants" with multiple "Workers" each performing a specific task related to bottling juice
The workers can perform one of the following 4 tasks
- Fetch
- Peel
- Squeeze
- Bottle
- Process
Methods for workers to access the oranges to complete their task have been synchronized so that we do not have two workers trying to take and process the same orange
Workers are created in groups of 6, with multiple workers created to Peel and Squeeze the oranges since these are our most time-intensive tasks  

To run the project simply install Ant, open a terminal and run the following command:  
    `ant run`  
OR install java, navigate to src/, and run the following command:  
    `java Plant.java`  

