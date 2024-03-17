CREATE OR REPLACE LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_stock() RETURNS TRIGGER AS $$
BEGIN
	UPDATE Product
	SET numberOfUnits = numberOfUnits - NEW.unitsOrdered
	WHERE storeID = NEW.storeID AND productName = NEW.productName;
	RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_update_stock ON Orders;

CREATE TRIGGER trg_update_stock
AFTER INSERT ON Orders
FOR EACH ROW
	EXECUTE PROCEDURE update_stock();
