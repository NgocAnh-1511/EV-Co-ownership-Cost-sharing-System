-- Migration script to remove vehicle_count column from vehiclegroup table
-- Date: 2024
-- Description: Remove vehicle_count column as each group now has exactly 1 vehicle

-- Step 1: Remove the vehicle_count column from vehiclegroup table
ALTER TABLE vehicle_management.vehiclegroup
DROP COLUMN IF EXISTS vehicle_count;

-- Verify the column has been removed
-- SELECT COLUMN_NAME 
-- FROM INFORMATION_SCHEMA.COLUMNS 
-- WHERE TABLE_SCHEMA = 'vehicle_management' 
--   AND TABLE_NAME = 'vehiclegroup' 
--   AND COLUMN_NAME = 'vehicle_count';
-- Should return no rows if successful





