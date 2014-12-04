parallel_regression
===================

PJ2 library

This program uses the PJ2 library for parallel programming, located at
http://www.cs.rit.edu/~ark/pj2.shtml.

The paradigm runs on a multicore or multinode cluster for running Tasks
and Jobs.  This program is used for logistic regression on massive 
datasets.  It can be used in both sequential and parallel verisons,
and the parallel version can be set to batched training, based off 
of the following paper: 

Mu Li, Tong Zhang, Yuqiang Chen, and Alexander J. Smola. 2014. Efficient 
mini-batch training for stochastic optimization. In Proceedings of the 
20th ACM SIGKDD international conference on Knowledge discovery and data 
mining (KDD '14). ACM, New York, NY, USA, 661-670. 
URL:http://dl.acm.org/citation.cfm?id=2623330.2623612&coll=DL&dl=ACM&CFID=415399891&CFTOKEN=69514427

The URLDataStrategy class is a class specifically implemented for the large 
URL Reputation dataset, archived at https://archive.ics.uci.edu/ml/datasets/URL+Reputation.
This dataset is extremely sparse and stored in SVM light format, so the URLDataStrategy 
implements some unique ways of performing the dot product.  Here is a link to the paper: 
Justin Ma, Lawrence K. Saul, Stefan Savage, and Geoffrey M. Voelker. 2009. 
Identifying suspicious URLs: an application of large-scale online learning. 
In Proceedings of the 26th Annual International Conference on Machine Learning 
(ICML '09). ACM, New York, NY, USA, 681-688. 
URL:http://cseweb.ucsd.edu/~savage/papers/ICML09.pdf

The RegularDataStrategy is the class that should be used for most logistic regression datasets, where 
all the data is in a single file, there are only two classes, the class decision is any of the rows 
(set this on the command line), and the classes are ordered sequentially (either 0/1, 1/2, 2/3, etc).

Using batch training on a 10 node cluster with all 3231961 features and 2396130 instances, batch training 
achieved 94% accuracy on a 20% test set after only approximately 6 minutes of training.  Stochaistic 
gradient descent on a single node achieved 97.4% accuracy in approximately 1 hour of training, using 
a parallelized for loop over updates to the weight arrray.  

Cluster description:
Frontend supervisory node — tardis.cs.rit.edu
Dual-core AMD Opteron 1214 processor
1.0 GHz clock
2 GB main memory
Ubuntu 12.04 LTS 64-bit Linux
Ten backend computational nodes — dr00 through dr09, each with:
Two AMD Opteron 2218 processors
Two CPU cores per processor
Four threads
2.6 GHz clock
8 GB main memory
Ubuntu 12.04 LTS 64-bit Linux
1-Gbps switched Ethernet backend interconnection network
Aggregate 40 threads, 80 GB main memory

To run the programs (note that Batched versions of both Sequential and 
Parallel regression can be run with a command-line argument):

****************Sequential version********************

Usage: java pj2 LogRegSeq <trainFile>
<trainFile> is a real-valued data file
#####################
The following are additional options available:
[seed=long] value to seed the prng, default is 1L
[testFile=file] specify test set, default is <trainFile>
[alpha=double] specify learning rate, default is 0.05
[sep=char] specify separator, default is whitespace
[class=int] specify class location, default is last column in row
[eps=double] specify convergence threshold, defaults to 0.000001
[steps=long] specify number of iterations instead of eps convergence
[thr=double] specify threhold of class decision, default is 0.5
[lambda=double] specify regularization parameter, default is 1.0
[snap=int] snapshot interval, default is 0
[records=int] number of records to process in the data, default is all data
[batched=int] number of batches to use, default is none
[gamma=double] gamma used for batch cost, default is all 100
[biter=int] iterations to use on one batch, default is 5
[test=double] percent (between 0-1) of train examples to set aside for test, default is 0
#####################

****************Parallel version********************

Usage: java pj2 LogRegSeq <trainFile> <chunked>
<trainFile> is a real-valued data file
<chunked> is boolean to specify the data was chunked into sub-files on cluster nodes
#####################
The following are additional options available:
[seed=long] value to seed the prng, default is 1L
[testFile=file] specify test set, default is <trainFile>
[alpha=double] specify learning rate, default is 0.05
[sep=char] specify separator, default is whitespace
[class=int] specify class location, default is last column in row
[eps=double] specify convergence threshold, defaults to 0.000001
[steps=long] specify number of iterations instead of eps convergence
[thr=double] specify threhold of class decision, default is 0.5
[lambda=double] specify regularization parameter, default is 1.0
[snap=int] snapshot interval, default is 0
[records=int] number of records to process in the data, default is all data
[batched=int] number of batches to use, default is none
[gamma=double] gamma used for batch cost, default is 100
[biter=int] iterations to use on one batch, default is 5
[test=double] percent (between 0-1) of train examples to set aside for test, default is 0
#####################

****************SGD Parallel version********************

Usage: java LogRegParSGD <trainFile>
<trainFile> is a real-valued data file
#####################
The following are additional options available:
[seed=long] value to seed the prng, default is 1L
[testFile=file] specify test set, default is <trainFile>
[alpha=double] specify learning rate, default is 0.05
[sep=char] specify separator, default is whitespace
[class=int] specify class location, default is last column in row
[eps=double] specify convergence threshold, defaults to 0.000001
[steps=long] specify number of iterations instead of eps convergence
[thr=double] specify threhold of class decision, default is 0.5
[snap=int] snapshot interval, default is 0
[records=int] number of records to process in the data, default is all data
[converge=int] number of steps before checking convergence, default is 500
[test=double] percent (between 0-1) of train examples to set aside for test, default is 0
#####################

****************SGD Sequential version********************

Usage: java LogRegSGD <trainFile>
<trainFile> is a real-valued data file
#####################
The following are additional options available:
[seed=long] value to seed the prng, default is 1L
[testFile=file] specify test set, default is <trainFile>
[alpha=double] specify learning rate, default is 0.05
[sep=char] specify separator, default is whitespace
[class=int] specify class location, default is last column in row
[eps=double] specify convergence threshold, defaults to 0.000001
[steps=long] specify number of iterations instead of eps convergence
[thr=double] specify threhold of class decision, default is 0.5
[snap=int] snapshot interval, default is 0
[records=int] number of records to process in the data, default is all data
[converge=int] number of steps before checking convergence, default is 500
[test=double] percent (between 0-1) of train examples to set aside for test, default is 0
#####################
