DELETE
FROM recipe_item_withdrawal
WHERE structure_uuid = ?
  AND inventory_type = ?;