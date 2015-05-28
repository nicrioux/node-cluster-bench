siege -c500 -t1M http://localhost:3000/

ab -n 1000 -c 1000 -vhr http://localhost:3000/