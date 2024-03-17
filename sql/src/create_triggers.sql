CREATE OR REPLACE LANGUAGE plpgsql;

-- Updates Parts.numberOfUnits after a customer places a successful order
CREATE OR REPLACE FUNCTION update_stock_after_order() RETURNS TRIGGER AS $$
BEGIN
	UPDATE Product
	SET numberOfUnits = numberOfUnits - NEW.unitsOrdered
	WHERE storeID = NEW.storeID AND productName = NEW.productName;
	RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_update_stock_after_order ON Orders;
CREATE TRIGGER trg_update_stock_after_order
AFTER INSERT ON Orders
FOR EACH ROW
	EXECUTE PROCEDURE update_stock_after_order();


-- Updates Parts.numberOfUnits after a customer places a successful order
CREATE OR REPLACE FUNCTION log_product_update() RETURNS TRIGGER AS $$
BEGIN
	INSERT INTO ProductUpdates(managerID, storeID, productName, updatedOn)
	VALUE (NEW.managerID, NEW.storeID, NEW.productName, NOW());
	RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_log_product_update ON Product;
CREATE TRIGGER trg_log_product_update
AFTER UPDATE ON Product
FOR EACH ROW
	EXECUTE PROCEDURE log_product_update();
