java pj2 jar=myjar.jar workers=1 LogRegPar URL true records=200000 steps=2000 test=0.2 >> strong_1_worker.txt 2>&1
echo "Testing echo"
java pj2 jar=myjar.jar workers=2 LogRegPar URL true records=200000 steps=2000 test=0.2 >> strong_2_worker.txt 2>&1
echo "Yup still working..."
java pj2 jar=myjar.jar workers=3 LogRegPar URL true records=200000 steps=2000 test=0.2 >> strong_3_worker.txt 2>&1
java pj2 jar=myjar.jar workers=4 LogRegPar URL true records=200000 steps=2000 test=0.2 >> strong_4_worker.txt 2>&1
java pj2 jar=myjar.jar workers=5 LogRegPar URL true records=200000 steps=2000 test=0.2 >> strong_5_worker.txt 2>&1
java pj2 jar=myjar.jar workers=6 LogRegPar URL true records=200000 steps=2000 test=0.2 >> strong_6_worker.txt 2>&1
java pj2 jar=myjar.jar workers=7 LogRegPar URL true records=200000 steps=2000 test=0.2 >> strong_7_worker.txt 2>&1
java pj2 jar=myjar.jar workers=8 LogRegPar URL true records=200000 steps=2000 test=0.2 >> strong_8_worker.txt 2>&1
echo "200k done"

java pj2 jar=myjar.jar workers=1 LogRegPar URL true records=100000 steps=2000 test=0.2 >> strong_1_worker.txt 2>&1
java pj2 jar=myjar.jar workers=2 LogRegPar URL true records=100000 steps=2000 test=0.2 >> strong_2_worker.txt 2>&1
java pj2 jar=myjar.jar workers=3 LogRegPar URL true records=100000 steps=2000 test=0.2 >> strong_3_worker.txt 2>&1
java pj2 jar=myjar.jar workers=4 LogRegPar URL true records=100000 steps=2000 test=0.2 >> strong_4_worker.txt 2>&1
java pj2 jar=myjar.jar workers=5 LogRegPar URL true records=100000 steps=2000 test=0.2 >> strong_5_worker.txt 2>&1
java pj2 jar=myjar.jar workers=6 LogRegPar URL true records=100000 steps=2000 test=0.2 >> strong_6_worker.txt 2>&1
java pj2 jar=myjar.jar workers=7 LogRegPar URL true records=100000 steps=2000 test=0.2 >> strong_7_worker.txt 2>&1
java pj2 jar=myjar.jar workers=8 LogRegPar URL true records=100000 steps=2000 test=0.2 >> strong_8_worker.txt 2>&1
echo "100k done"

java pj2 jar=myjar.jar workers=1 LogRegPar URL true records=75000 steps=2000 test=0.2 >> strong_1_worker.txt 2>&1
java pj2 jar=myjar.jar workers=2 LogRegPar URL true records=75000 steps=2000 test=0.2 >> strong_2_worker.txt 2>&1
java pj2 jar=myjar.jar workers=3 LogRegPar URL true records=75000 steps=2000 test=0.2 >> strong_3_worker.txt 2>&1
java pj2 jar=myjar.jar workers=4 LogRegPar URL true records=75000 steps=2000 test=0.2 >> strong_4_worker.txt 2>&1
java pj2 jar=myjar.jar workers=5 LogRegPar URL true records=75000 steps=2000 test=0.2 >> strong_5_worker.txt 2>&1
java pj2 jar=myjar.jar workers=6 LogRegPar URL true records=75000 steps=2000 test=0.2 >> strong_6_worker.txt 2>&1
java pj2 jar=myjar.jar workers=7 LogRegPar URL true records=75000 steps=2000 test=0.2 >> strong_7_worker.txt 2>&1
java pj2 jar=myjar.jar workers=8 LogRegPar URL true records=75000 steps=2000 test=0.2 >> strong_8_worker.txt 2>&1
echo "75k done"

java pj2 jar=myjar.jar workers=1 LogRegPar URL true records=50000 steps=2000 test=0.2 >> strong_1_worker.txt 2>&1
java pj2 jar=myjar.jar workers=2 LogRegPar URL true records=50000 steps=2000 test=0.2 >> strong_2_worker.txt 2>&1
java pj2 jar=myjar.jar workers=3 LogRegPar URL true records=50000 steps=2000 test=0.2 >> strong_3_worker.txt 2>&1
java pj2 jar=myjar.jar workers=4 LogRegPar URL true records=50000 steps=2000 test=0.2 >> strong_4_worker.txt 2>&1
java pj2 jar=myjar.jar workers=5 LogRegPar URL true records=50000 steps=2000 test=0.2 >> strong_5_worker.txt 2>&1
java pj2 jar=myjar.jar workers=6 LogRegPar URL true records=50000 steps=2000 test=0.2 >> strong_6_worker.txt 2>&1
java pj2 jar=myjar.jar workers=7 LogRegPar URL true records=50000 steps=2000 test=0.2 >> strong_7_worker.txt 2>&1
java pj2 jar=myjar.jar workers=8 LogRegPar URL true records=50000 steps=2000 test=0.2 >> strong_8_worker.txt 2>&1
echo "50k done"

java pj2 jar=myjar.jar workers=1 LogRegPar URL true records=30000 steps=2000 test=0.2 >> strong_1_worker.txt 2>&1
java pj2 jar=myjar.jar workers=2 LogRegPar URL true records=30000 steps=2000 test=0.2 >> strong_2_worker.txt 2>&1
java pj2 jar=myjar.jar workers=3 LogRegPar URL true records=30000 steps=2000 test=0.2 >> strong_3_worker.txt 2>&1
java pj2 jar=myjar.jar workers=4 LogRegPar URL true records=30000 steps=2000 test=0.2 >> strong_4_worker.txt 2>&1
java pj2 jar=myjar.jar workers=5 LogRegPar URL true records=30000 steps=2000 test=0.2 >> strong_5_worker.txt 2>&1
java pj2 jar=myjar.jar workers=6 LogRegPar URL true records=30000 steps=2000 test=0.2 >> strong_6_worker.txt 2>&1
java pj2 jar=myjar.jar workers=7 LogRegPar URL true records=30000 steps=2000 test=0.2 >> strong_7_worker.txt 2>&1
java pj2 jar=myjar.jar workers=8 LogRegPar URL true records=30000 steps=2000 test=0.2 >> strong_8_worker.txt 2>&1
echo "DONE"
