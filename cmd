awk '{if ($1=="=+=") print $2,$3}' < ii > jj
echo "plot \"jj\" " | gnuplot -geometry 255x255+0+640 -persist

