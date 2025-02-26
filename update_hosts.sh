#!/bin/bash

# Check if running as root
if [ "$EUID" -ne 0 ]; then
  echo "Please run this script as root or using sudo."
  exit 1
fi

# Set hosts file path
hosts_path="/etc/hosts"

# Define domain mappings to add
domains=(
  "127.0.0.1 rymcu.local"
  "127.0.0.1 logto.rymcu.local"
  "127.0.0.1 auth.rymcu.local"
  "127.0.0.1 npm.rymcu.local"
)

# Function to add domain if it doesn't exist
add_domain() {
  local domain=$1
  if grep -Fxq "$domain" "$hosts_path"; then
    echo "\"$domain\" already exists, skipping."
  else
    echo "Adding \"$domain\"..."
    echo "$domain" | sudo tee -a "$hosts_path" > /dev/null
  fi
}

# Iterate through each domain and add it if necessary
for domain in "${domains[@]}"; do
  add_domain "$domain"
done

echo "Operation completed."
