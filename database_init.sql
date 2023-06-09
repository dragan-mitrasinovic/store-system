
CREATE TABLE [Article]
( 
	[price]              integer  NULL 
	CONSTRAINT [GreaterThenZero_1335949298]
		CHECK  ( price >= 1 ),
	[name]               varchar(100)  NULL ,
	[articleId]          integer  IDENTITY  NOT NULL ,
	[shopId]             integer  NOT NULL ,
	[amount]             integer  NULL 
	CONSTRAINT [Zero_1464554507]
		 DEFAULT  0
	CONSTRAINT [GreaterThenOrEqualToZero_1599883297]
		CHECK  ( amount >= 0 )
)
go

CREATE TABLE [ArticleInOrder]
( 
	[itemId]             integer  IDENTITY  NOT NULL ,
	[orderId]            integer  NOT NULL ,
	[articleId]          integer  NOT NULL ,
	[count]              integer  NULL 
)
go

CREATE TABLE [Buyer]
( 
	[buyerId]            integer  IDENTITY  NOT NULL ,
	[name]               varchar(100)  NULL ,
	[cityId]             integer  NOT NULL ,
	[credit]             decimal(10,3)  NULL 
	CONSTRAINT [Zero_629060815]
		 DEFAULT  0
	CONSTRAINT [GreaterThenOrEqualToZero_493732025]
		CHECK  ( credit >= 0 )
)
go

CREATE TABLE [City]
( 
	[cityId]             integer  IDENTITY  NOT NULL ,
	[name]               varchar(100)  NULL 
)
go

CREATE TABLE [Line]
( 
	[lineId]             integer  IDENTITY  NOT NULL ,
	[cityId1]            integer  NOT NULL ,
	[cityId2]            integer  NOT NULL ,
	[distance]           integer  NULL 
	CONSTRAINT [GreaterThenZero_1335619834]
		CHECK  ( distance >= 1 )
)
go

CREATE TABLE [OrderT]
( 
	[buyerId]            integer  NOT NULL ,
	[orderId]            integer  IDENTITY  NOT NULL ,
	[state]              varchar(10)  NULL 
	CONSTRAINT [Created_1634600188]
		 DEFAULT  'created'
	CONSTRAINT [CreatedSentOrArrived_752791273]
		CHECK  ( [state]='created' OR [state]='sent' OR [state]='arrived' ),
	[sentTime]           datetime  NULL ,
	[receivedTime]       datetime  NULL ,
	[location]           integer  NULL ,
	[finalPrice]         decimal(10,3)  NULL 
	CONSTRAINT [GreaterThenOrEqualToZero_186246947]
		CHECK  ( finalPrice >= 0 ),
	[discountSum]        decimal(10,3)  NULL 
	CONSTRAINT [GreaterThenOrEqualToZero_253740951]
		CHECK  ( discountSum >= 0 ),
	[systemProfit]       decimal(10,3)  NULL 
	CONSTRAINT [GreaterThenOrEqualToZero_16290438]
		CHECK  ( systemProfit >= 0 ),
	[nextCity]           integer  NULL ,
	[timeToNext]         integer  NULL 
	CONSTRAINT [GreaterThenOrEqualToZero_807001077]
		CHECK  ( timeToNext >= 0 )
)
go

CREATE TABLE [Shop]
( 
	[shopId]             integer  IDENTITY  NOT NULL ,
	[name]               varchar(100)  NULL ,
	[cityId]             integer  NOT NULL ,
	[discount]           integer  NULL 
	CONSTRAINT [Zero_2119384137]
		 DEFAULT  0
	CONSTRAINT [Percentage_940393600]
		CHECK  ( discount BETWEEN 0 AND 100 )
)
go

CREATE TABLE [TransactionT]
( 
	[transactionId]      integer  IDENTITY  NOT NULL ,
	[buyerId]            integer  NULL ,
	[orderId]            integer  NOT NULL ,
	[shopId]             integer  NULL ,
	[type]               varchar(10)  NULL 
	CONSTRAINT [BuyerOrShopOrSystem_413772262]
		CHECK  ( [type]='buyer' OR [type]='shop' OR [type]='system' ),
	[executionTime]      datetime  NULL ,
	[amount]             decimal(10,3)  NULL 
)
go

ALTER TABLE [Article]
	ADD CONSTRAINT [XPKArticle] PRIMARY KEY  CLUSTERED ([articleId] ASC)
go

ALTER TABLE [ArticleInOrder]
	ADD CONSTRAINT [XPKArticleInOrder] PRIMARY KEY  CLUSTERED ([itemId] ASC)
go

ALTER TABLE [ArticleInOrder]
	ADD CONSTRAINT [XAK1ArticleInOrder] UNIQUE ([articleId]  ASC,[orderId]  ASC)
go

ALTER TABLE [Buyer]
	ADD CONSTRAINT [XPKBuyer] PRIMARY KEY  CLUSTERED ([buyerId] ASC)
go

ALTER TABLE [City]
	ADD CONSTRAINT [XPKCity] PRIMARY KEY  CLUSTERED ([cityId] ASC)
go

ALTER TABLE [City]
	ADD CONSTRAINT [XAK1City] UNIQUE ([name]  ASC)
go

ALTER TABLE [Line]
	ADD CONSTRAINT [XPKLine] PRIMARY KEY  CLUSTERED ([lineId] ASC)
go

ALTER TABLE [Line]
	ADD CONSTRAINT [XAK1Line] UNIQUE ([cityId1]  ASC,[cityId2]  ASC)
go

ALTER TABLE [OrderT]
	ADD CONSTRAINT [XPKOrder] PRIMARY KEY  CLUSTERED ([orderId] ASC)
go

ALTER TABLE [Shop]
	ADD CONSTRAINT [XPKShop] PRIMARY KEY  CLUSTERED ([shopId] ASC)
go

ALTER TABLE [Shop]
	ADD CONSTRAINT [XAK1Shop] UNIQUE ([name]  ASC)
go

ALTER TABLE [TransactionT]
	ADD CONSTRAINT [XPKTransaction] PRIMARY KEY  CLUSTERED ([transactionId] ASC)
go


ALTER TABLE [Article]
	ADD CONSTRAINT [R_4] FOREIGN KEY ([shopId]) REFERENCES [Shop]([shopId])
		ON DELETE NO ACTION
		ON UPDATE CASCADE
go


ALTER TABLE [ArticleInOrder]
	ADD CONSTRAINT [R_8] FOREIGN KEY ([orderId]) REFERENCES [OrderT]([orderId])
		ON DELETE CASCADE
		ON UPDATE CASCADE
go

ALTER TABLE [ArticleInOrder]
	ADD CONSTRAINT [R_9] FOREIGN KEY ([articleId]) REFERENCES [Article]([articleId])
		ON DELETE NO ACTION
		ON UPDATE CASCADE
go


ALTER TABLE [Buyer]
	ADD CONSTRAINT [R_6] FOREIGN KEY ([cityId]) REFERENCES [City]([cityId])
		ON DELETE NO ACTION
		ON UPDATE CASCADE
go


ALTER TABLE [Line]
	ADD CONSTRAINT [R_11] FOREIGN KEY ([cityId1]) REFERENCES [City]([cityId])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go

ALTER TABLE [Line]
	ADD CONSTRAINT [R_12] FOREIGN KEY ([cityId2]) REFERENCES [City]([cityId])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


ALTER TABLE [OrderT]
	ADD CONSTRAINT [R_7] FOREIGN KEY ([buyerId]) REFERENCES [Buyer]([buyerId])
		ON DELETE NO ACTION
		ON UPDATE CASCADE
go

ALTER TABLE [OrderT]
	ADD CONSTRAINT [R_10] FOREIGN KEY ([location]) REFERENCES [City]([cityId])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go

ALTER TABLE [OrderT]
	ADD CONSTRAINT [R_19] FOREIGN KEY ([nextCity]) REFERENCES [City]([cityId])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


ALTER TABLE [Shop]
	ADD CONSTRAINT [R_5] FOREIGN KEY ([cityId]) REFERENCES [City]([cityId])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


ALTER TABLE [TransactionT]
	ADD CONSTRAINT [R_13] FOREIGN KEY ([buyerId]) REFERENCES [Buyer]([buyerId])
		ON DELETE NO ACTION
		ON UPDATE CASCADE
go

ALTER TABLE [TransactionT]
	ADD CONSTRAINT [R_14] FOREIGN KEY ([orderId]) REFERENCES [OrderT]([orderId])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go

ALTER TABLE [TransactionT]
	ADD CONSTRAINT [R_15] FOREIGN KEY ([shopId]) REFERENCES [Shop]([shopId])
		ON DELETE NO ACTION
		ON UPDATE CASCADE
go

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