UPDATE inventory_content
SET item_content = ?
WHERE structure_uuid = ?
  AND inventory_type = ?
  AND pos = ?;