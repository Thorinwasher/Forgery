DELETE
FROM inventory
WHERE structure_uuid = ?
  AND inventory_type = ?;