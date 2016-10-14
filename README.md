# DistributedDatabaseSimulator

To start, you must use a local MySQL database. There is the JDBC driver for that included in this project.<br/>
Run the CreateDatabase.sql script to set up the DB.<br/>
<br/>
To set up a simulation change the parameters in the file: params.txt<br/>
<br/>
It MUST be in this format:<br/>
<br/>
SEED:432455733454<br/>
Topology:HyperCube<br/>
NumPages:1000<br/>
ArrivalRate:75<br/>
DDP:AgentDeadlockDetectionProtocol,TimeoutDeadlockDetection<br/>
DRP:AgentDeadlockResolutionProtocol<br/>
<br/>
This will run 2 simulations, one with AgentDeadlockDetectionProtocol and one with TimeoutDeadlockDetection.<br/>
Use commas to separate variations.<br/>
<br/>
The results will be inserted into a local MySQL database .
