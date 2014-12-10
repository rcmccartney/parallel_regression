java pj2 jar=myjar.jar workers=1 LogRegPar URL true records=200000 steps=2000 test=0.2 >> weak_1_worker.txt 2>&1
java pj2 jar=myjar.jar workers=2 LogRegPar URL true records=400000 steps=2000 test=0.2 >> weak_2_worker.txt 2>&1
java pj2 jar=myjar.jar workers=3 LogRegPar URL true records=600000 steps=2000 test=0.2 >> weak_3_worker.txt 2>&1
java pj2 jar=myjar.jar workers=4 LogRegPar URL true records=800000 steps=2000 test=0.2 >> weak_4_worker.txt 2>&1
java pj2 jar=myjar.jar workers=5 LogRegPar URL true records=1000000 steps=2000 test=0.2 >> weak_5_worker.txt 2>&1
java pj2 jar=myjar.jar workers=6 LogRegPar URL true records=1200000 steps=2000 test=0.2 >> weak_6_worker.txt 2>&1
java pj2 jar=myjar.jar workers=7 LogRegPar URL true records=1400000 steps=2000 test=0.2 >> weak_7_worker.txt 2>&1
java pj2 jar=myjar.jar workers=8 LogRegPar URL true records=1600000 steps=2000 test=0.2 >> weak_8_worker.txt 2>&1
echo "200k done"

java pj2 jar=myjar.jar workers=1 LogRegPar URL true records=150000 steps=2000 test=0.2 >> weak_1_worker.txt 2>&1
java pj2 jar=myjar.jar workers=2 LogRegPar URL true records=300000 steps=2000 test=0.2 >> weak_2_worker.txt 2>&1
java pj2 jar=myjar.jar workers=3 LogRegPar URL true records=450000 steps=2000 test=0.2 >> weak_3_worker.txt 2>&1
java pj2 jar=myjar.jar workers=4 LogRegPar URL true records=600000 steps=2000 test=0.2 >> weak_4_worker.txt 2>&1
java pj2 jar=myjar.jar workers=5 LogRegPar URL true records=750000 steps=2000 test=0.2 >> weak_5_worker.txt 2>&1
java pj2 jar=myjar.jar workers=6 LogRegPar URL true records=900000 steps=2000 test=0.2 >> weak_6_worker.txt 2>&1
java pj2 jar=myjar.jar workers=7 LogRegPar URL true records=1050000 steps=2000 test=0.2 >> weak_7_worker.txt 2>&1
java pj2 jar=myjar.jar workers=8 LogRegPar URL true records=1200000 steps=2000 test=0.2 >> weak_8_worker.txt 2>&1
echo "150k done"

java pj2 jar=myjar.jar workers=1 LogRegPar URL true records=100000 steps=2000 test=0.2 >> weak_1_worker.txt 2>&1
java pj2 jar=myjar.jar workers=2 LogRegPar URL true records=200000 steps=2000 test=0.2 >> weak_2_worker.txt 2>&1
java pj2 jar=myjar.jar workers=3 LogRegPar URL true records=300000 steps=2000 test=0.2 >> weak_3_worker.txt 2>&1
java pj2 jar=myjar.jar workers=4 LogRegPar URL true records=400000 steps=2000 test=0.2 >> weak_4_worker.txt 2>&1
java pj2 jar=myjar.jar workers=5 LogRegPar URL true records=500000 steps=2000 test=0.2 >> weak_5_worker.txt 2>&1
java pj2 jar=myjar.jar workers=6 LogRegPar URL true records=600000 steps=2000 test=0.2 >> weak_6_worker.txt 2>&1
java pj2 jar=myjar.jar workers=7 LogRegPar URL true records=700000 steps=2000 test=0.2 >> weak_7_worker.txt 2>&1
java pj2 jar=myjar.jar workers=8 LogRegPar URL true records=800000 steps=2000 test=0.2 >> weak_8_worker.txt 2>&1
echo "100k done"

java pj2 jar=myjar.jar workers=1 LogRegPar URL true records=50000 steps=2000 test=0.2 >> weak_1_worker.txt 2>&1
java pj2 jar=myjar.jar workers=2 LogRegPar URL true records=100000 steps=2000 test=0.2 >> weak_2_worker.txt 2>&1
java pj2 jar=myjar.jar workers=3 LogRegPar URL true records=150000 steps=2000 test=0.2 >> weak_3_worker.txt 2>&1
java pj2 jar=myjar.jar workers=4 LogRegPar URL true records=200000 steps=2000 test=0.2 >> weak_4_worker.txt 2>&1
java pj2 jar=myjar.jar workers=5 LogRegPar URL true records=250000 steps=2000 test=0.2 >> weak_5_worker.txt 2>&1
java pj2 jar=myjar.jar workers=6 LogRegPar URL true records=300000 steps=2000 test=0.2 >> weak_6_worker.txt 2>&1
java pj2 jar=myjar.jar workers=7 LogRegPar URL true records=350000 steps=2000 test=0.2 >> weak_7_worker.txt 2>&1
java pj2 jar=myjar.jar workers=8 LogRegPar URL true records=400000 steps=2000 test=0.2 >> weak_8_worker.txt 2>&1
echo "50k done"

java pj2 jar=myjar.jar workers=1 LogRegPar URL true records=25000 steps=2000 test=0.2 >> weak_1_worker.txt 2>&1
java pj2 jar=myjar.jar workers=2 LogRegPar URL true records=50000 steps=2000 test=0.2 >> weak_2_worker.txt 2>&1
java pj2 jar=myjar.jar workers=3 LogRegPar URL true records=75000 steps=2000 test=0.2 >> weak_3_worker.txt 2>&1
java pj2 jar=myjar.jar workers=4 LogRegPar URL true records=100000 steps=2000 test=0.2 >> weak_4_worker.txt 2>&1
java pj2 jar=myjar.jar workers=5 LogRegPar URL true records=125000 steps=2000 test=0.2 >> weak_5_worker.txt 2>&1
java pj2 jar=myjar.jar workers=6 LogRegPar URL true records=150000 steps=2000 test=0.2 >> weak_6_worker.txt 2>&1
java pj2 jar=myjar.jar workers=7 LogRegPar URL true records=175000 steps=2000 test=0.2 >> weak_7_worker.txt 2>&1
java pj2 jar=myjar.jar workers=8 LogRegPar URL true records=200000 steps=2000 test=0.2 >> weak_8_worker.txt 2>&1
echo "DONE"
