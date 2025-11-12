-- Migration script to add unique constraint on group_id in vehicle table
-- Date: 2024
-- Description: Add unique constraint to ensure one vehicle per group (one-to-one relationship)

-- Step 1: Check if there are any groups with multiple vehicles
-- This query will show groups that have more than 1 vehicle
-- Run this first to check data integrity
SELECT group_id, COUNT(*) as vehicle_count
FROM vehicle_management.vehicle
GROUP BY group_id
HAVING COUNT(*) > 1;

-- If the above query returns any rows, you need to clean up the data first
-- Delete duplicate vehicles or move them to different groups before adding the constraint

-- Step 2: Remove any duplicate vehicles (keep only one vehicle per group)
-- WARNING: This will delete vehicles. Make sure to backup your data first!
-- Uncomment and run only if you need to clean up duplicates
/*
DELETE v1 FROM vehicle_management.vehicle v1
INNER JOIN vehicle_management.vehicle v2
WHERE v1.vehicle_id > v2.vehicle_id
  AND v1.group_id = v2.group_id;
*/

-- Step 3: Add unique constraint on group_id
-- This ensures that each group can only have one vehicle
ALTER TABLE vehicle_management.vehicle
ADD CONSTRAINT uk_vehicle_group_id UNIQUE (group_id);

-- Step 4: Make group_id NOT NULL (if not already)
-- This ensures that every vehicle must belong to a group
ALTER TABLE vehicle_management.vehicle
MODIFY COLUMN group_id VARCHAR(20) NOT NULL;

-- Verify the constraint has been added
-- SELECT CONSTRAINT_NAME, CONSTRAINT_TYPE
-- FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
-- WHERE TABLE_SCHEMA = 'vehicle_management'
--   AND TABLE_NAME = 'vehicle'
--   AND CONSTRAINT_NAME = 'uk_vehicle_group_id';
-- Should return one row with CONSTRAINT_TYPE = 'UNIQUE'

-- Verify the column is NOT NULL
-- SELECT COLUMN_NAME, IS_NULLABLE
-- FROM INFORMATION_SCHEMA.COLUMNS
-- WHERE TABLE_SCHEMA = 'vehicle_management'
--   AND TABLE_NAME = 'vehicle'
--   AND COLUMN_NAME = 'group_id';
-- Should return IS_NULLABLE = 'NO'






