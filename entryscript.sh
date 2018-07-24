#!bin/bash



if [ -z ${1+x} ]; then \
  bash;
else exec $1; fi
