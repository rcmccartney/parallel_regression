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
