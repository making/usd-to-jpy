#!/bin/bash
#set -x
# Function to create a directory for a given date, output the date, and perform a curl request
dump() {
  # Extract year, month, and day
  year=$(date -d "$1" '+%Y')
  yy=$(date -d "$1" '+%y')
  month=$(date -d "$1" '+%m')
  day=$(date -d "$1" '+%d')

  # Output the date in yymmdd format
  echo "${yy}${month}${day}"

  # Perform a curl request to a specified URL
  url="https://www.murc-kawasesouba.jp/fx/past/index.php?id=${yy}${month}${day}"
  curl --fail --silent "$url" > /tmp/quote.txt
  if [ $? -eq 0 ]; then
    # If curl succeeds, continue
    cat /tmp/quote.txt | grep USD -m 1 -A 2| grep -oP '(?<=t_right">)\d+\.\d+(?= )' > /tmp/usd.txt
    # Create directory in the format yyyy/mm/dd
    mkdir -p "$year/$month/$day"
    TTS=$(cat /tmp/usd.txt | sed -n '1p' | tr -d '\r' | tr -d '\n')
    if [ -z $TTS ]; then
      return
    fi
    TTB=$(cat /tmp/usd.txt | sed -n '2p' | tr -d '\r' | tr -d '\n')
    TTM=$(awk "BEGIN {printf \"%.2f\", ($TTS + $TTB) / 2}")
    echo -n $TTS > $year/$month/$day/TTS
    echo -n $TTM > $year/$month/$day/TTM
    echo -n $TTB > $year/$month/$day/TTB
    rm -f /tmp/quote.txt /tmp/usd.txt
  else
    # If curl returns 404 or other errors, return from the function
    echo "Curl request failed for $url with error"
    return
  fi
}

# Starting date
start_date="2025-01-17"

# Ending date
end_date="2025-12-31"

# Iterate over each day
current_date=$start_date
while [[ "$current_date" != "$end_date" ]]; do
  dump "$current_date"
  # Increment date by one day
  current_date=$(date -I -d "$current_date + 1 day")
done

# Handle the last date
dump "$end_date"

