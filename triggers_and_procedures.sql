CREATE PROCEDURE SP_FINAL_PRICE 
	@OrderId int,
	@CurrentDate date
AS
BEGIN

DECLARE @FinalPrice decimal(10,3)
DECLARE @Discount decimal(10,3)
DECLARE @BuyerId int
DECLARE @AmountSpentLastMonth decimal(10,3)
DECLARE @SystemProfit decimal(10,3)

Declare @CountPriceDiscount table
(
	finalPrice decimal(10,3),
	discount decimal(10,3)
)
	
INSERT INTO @CountPriceDiscount(finalPrice, discount)
SELECT  Aio.count * A.price * (1.0 - S.discount / 100.0) as finalPrice, Aio.count * A.price * (S.discount / 100.0) as discount
FROM ArticleInOrder AiO 
JOIN Article A ON (AiO.articleId = A.articleId)
JOIN Shop S ON (A.shopId = S.shopId)
WHERE orderId = @OrderId

SELECT @FinalPrice = SUM(finalPrice), @Discount = SUM(discount)
FROM @CountPriceDiscount

SELECT @BuyerId = buyerId
FROM OrderT
WHERE orderId = @OrderId

SELECT @AmountSpentLastMonth = SUM(T.amount)
FROM TransactionT T
WHERE buyerId = @BuyerId AND
DATEDIFF(DAY, T.executionTime, @CurrentDate) < 30 AND
T.type = 'buyer'

IF @AmountSpentLastMonth > 10000
BEGIN
	SET @SystemProfit = @FinalPrice * 0.03
	SET @Discount = @Discount + @FinalPrice * 0.02
	SET @FinalPrice = @FinalPrice * 0.98
END
ELSE
BEGIN
	SET @SystemProfit = @FinalPrice * 0.05
END

UPDATE OrderT
SET finalPrice = @FinalPrice,
	discountSum = @Discount,
	systemProfit = @SystemProfit
WHERE orderId = @OrderId

END
GO


CREATE TRIGGER TR_TRANSFER_MONEY_TO_SHOPS
   ON TransactionT
   FOR INSERT
AS 
BEGIN

Declare @SystemTransactionsCursor cursor
DECLARE @OrderId int
DECLARE @ExecutionTime date
DECLARE @ShopTransactions table
(
	shopId int,
	price decimal(10, 3)
)
DECLARE @GroupedShopTransactions table
(
	shopId int,
	price decimal(10, 3)
)

SET @SystemTransactionsCursor = CURSOR FOR
SELECT orderId, executionTime
FROM inserted
WHERE type = 'system'

OPEN @SystemTransactionsCursor
FETCH NEXT FROM @SystemTransactionsCursor
INTO @OrderId, @ExecutionTime

WHILE @@FETCH_STATUS = 0
BEGIN
	INSERT INTO @ShopTransactions(shopId, price)
	SELECT  S.shopId, Aio.count * A.price * (1.0 - S.discount / 100.0) as price
	FROM ArticleInOrder AiO 
	JOIN Article A ON (AiO.articleId = A.articleId)
	JOIN Shop S ON (A.shopId = S.shopId)
	WHERE orderId = @OrderId

	INSERT INTO @GroupedShopTransactions(shopId, price)
	SELECT shopId, SUM(price) * 0.95
	FROM @ShopTransactions
	GROUP BY shopId

	DECLARE @ShopCursor cursor
	DECLARE @ShopId int
	DECLARE @Price decimal(10, 3)

	SET @ShopCursor = CURSOR FOR
	SELECT shopId, price
	FROM @GroupedShopTransactions

	OPEN @ShopCursor
	FETCH NEXT FROM @ShopCursor
	INTO @ShopId, @Price

	WHILE @@FETCH_STATUS = 0
	BEGIN
		INSERT INTO TransactionT(orderId, shopId, type, executionTime, amount)
		VALUES(@OrderId, @ShopId, 'shop', @ExecutionTime, @Price)

		FETCH NEXT FROM @ShopCursor
		INTO @ShopId, @Price
	END

	CLOSE @ShopCursor
	DEALLOCATE @ShopCursor
	FETCH NEXT FROM @SystemTransactionsCursor
	INTO @OrderId, @ExecutionTime
END

CLOSE @SystemTransactionsCursor
DEALLOCATE @SystemTransactionsCursor

END
GO