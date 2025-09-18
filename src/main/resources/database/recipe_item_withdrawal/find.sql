SELECT withdrawn_amount, inventory_type
FROM recipe_item_withdrawal
WHERE structure_uuid = ?;