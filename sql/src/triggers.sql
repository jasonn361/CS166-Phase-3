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


-- Inserts the update to a product to the ProductUpdate table
CREATE OR REPLACE FUNCTION log_product_update() RETURNS TRIGGER AS $$
DECLARE
	currentManagerID integer;
BEGIN
	SELECT managerID INTO currentManagerID FROM Store WHERE storeID = NEW.storeID;
	
	IF FOUND THEN
		INSERT INTO ProductUpdates(managerID, storeID, productName, updatedOn)
		VALUES (currentManagerID, NEW.storeID, NEW.productName, NOW());
	END IF;
	RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_log_product_update ON Product;
CREATE TRIGGER trg_log_product_update
AFTER UPDATE ON Product
FOR EACH ROW
	EXECUTE PROCEDURE log_product_update();
