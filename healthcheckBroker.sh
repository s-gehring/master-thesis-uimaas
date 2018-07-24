#!/bin/bash

curl -sS localhost:8080 | grep "No clientID header specified"
