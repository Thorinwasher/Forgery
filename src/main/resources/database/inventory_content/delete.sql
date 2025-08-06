DELETE
FROM inventory_content
WHERE inventory_type = ?
  AND structure_uuid = ?
  AND pos = ?;