import csv
import random
import time
import os
from datetime import datetime, timedelta

# --- CONFIGURATION ---
# List of Device IDs (UUIDs) you want to simulate
DEVICE_IDS = [
    "2e36b7c5-c25e-4390-adb8-fd5505161fda",
]

# Simulation Settings
DAYS_TO_SIMULATE = 7
START_DATE = datetime(2025, 11, 23, 0, 0, 0) # Start from
INTERVAL_MINUTES = 10

# Output File Path (Adjust as needed)
# This attempts to save it directly to the resources folder if run from the 'device_simulator' dir
OUTPUT_FILE = "../../../../../resources/sensor.csv" 

def generate_data():
    print(f"Generating data for {len(DEVICE_IDS)} devices over {DAYS_TO_SIMULATE} days...")
    
    # Ensure directory exists
    os.makedirs(os.path.dirname(OUTPUT_FILE), exist_ok=True)

    with open(OUTPUT_FILE, mode='w', newline='') as file:
        writer = csv.writer(file)
        
        # Iterate through time
        current_time = START_DATE
        end_time = START_DATE + timedelta(days=DAYS_TO_SIMULATE)
        
        total_records = 0

        while current_time < end_time:
            # Convert to epoch milliseconds
            timestamp = int(current_time.timestamp() * 1000)
            
            for device_id in DEVICE_IDS:
                # Generate a somewhat realistic value
                # Base value + random fluctuation + time of day factor
                hour = current_time.hour
                
                # Morning spike (7-9), Evening spike (18-22)
                if 7 <= hour <= 9:
                    base_load = 5.0
                elif 18 <= hour <= 22:
                    base_load = 8.0
                else:
                    base_load = 2.0
                
                fluctuation = random.uniform(-1.0, 1.5)
                measurement = max(0.1, round(base_load + fluctuation, 2))
                
                writer.writerow([timestamp, device_id, measurement])
                total_records += 1
            
            # Advance time
            current_time += timedelta(minutes=INTERVAL_MINUTES)

    print(f"Done! Generated {total_records} records in {OUTPUT_FILE}")

if __name__ == "__main__":
    generate_data()