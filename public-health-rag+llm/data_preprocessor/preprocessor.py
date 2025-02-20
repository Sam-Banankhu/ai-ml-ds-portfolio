import pandas as pd
import uuid
import json

# Define the CSV file path
csv_file_path = "data/health_facilities/facilities_data.csv"

# Load the CSV into a pandas DataFrame
df = pd.read_csv(csv_file_path)

# Initialize dictionaries for districts and facilities
districts = {}
facilities = []

# Generate UUIDs for districts and facilities and populate the data
for _, row in df.iterrows():
    district_name = row["District"]
    
    # Check if the district is already added, if not add it with a UUID
    if district_name not in districts:
        district_uuid = str(uuid.uuid4())
        districts[district_name] = {
            "id": district_uuid,
            "name": district_name
        }
    
    # Create a facility entry
    facility_uuid = str(uuid.uuid4())
    facility_data = {
        "id": facility_uuid,
        "name": row["Facility Name"],
        "facility_type": row["Facility Type"],
        "managing_authority": row["Managing Authority"],
        "urban_rural": row["Urban/Rural"],
        "district_id": districts[district_name]["id"]
    }
    facilities.append(facility_data)

# Convert districts to a list for easier serialization
districts_list = list(districts.values())

# Prepare the final data structure
output_data = {
    "districts": districts_list,
    "facilities": facilities
}

# Write the output to a JSON file
output_file_path = "data/health_facilities/districts_facilities_data.json"
with open(output_file_path, "w") as json_file:
    json.dump(output_data, json_file, indent=4)

print(f"Data has been successfully transformed and saved to {output_file_path}")
