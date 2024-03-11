#!/bin/bash
#set -x
# Function to create a directory for a given date, output the date, and perform a curl request
dump() {
  # Extract year, month, and day
  year=$(date -d "$1" '+%Y')
  month=$(date -d "$1" '+%m')
  day=$(date -d "$1" '+%d')

  # Output the date in yyyy-mm-dd format
  echo "$year-$month-$day"

  # Perform a curl request to a specified URL
  url="https://www.mizuhobank.co.jp/market/quote/data/quote_${year}${month}${day}.txt"
  curl --fail --silent "$url" > /tmp/quote.txt
  if [ $? -eq 0 ]; then
    # If curl succeeds, continue
    iconv -f SHIFT_JISX0213 -t UTF-8 /tmp/quote.txt | grep USD > /tmp/usd.txt
    # Create directory in the format yyyy/mm/dd
    mkdir -p "$year/$month/$day"
    cat /tmp/usd.txt | awk '{print $3}' | tr -d '\r' | tr -d '\n' > $year/$month/$day/TTS
    cat /tmp/usd.txt | awk '{print $4}' | tr -d '\r' | tr -d '\n' > $year/$month/$day/TTB
    cat /tmp/usd.txt | awk '{print $5}' | tr -d '\r' | tr -d '\n' > $year/$month/$day/TTM
    rm -f /tmp/quote.txt /tmp/usd.txt
  else
    # If curl returns 404 or other errors, return from the function
    echo "Curl request failed for $url with error"
    return
  fi
}

# Starting date
start_date="2023-12-23"

# Ending date
end_date="2024-03-10"

# Iterate over each day
current_date=$start_date
while [[ "$current_date" != "$end_date" ]]; do
  dump "$current_date"
  # Increment date by one day
  current_date=$(date -I -d "$current_date + 1 day")
done

# Handle the last date
dump "$end_date"

