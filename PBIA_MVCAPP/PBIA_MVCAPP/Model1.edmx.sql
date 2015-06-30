
-- --------------------------------------------------
-- Entity Designer DDL Script for SQL Server 2005, 2008, and Azure
-- --------------------------------------------------
-- Date Created: 06/30/2015 01:35:40
-- Generated from EDMX file: F:\Repozytorium\BezpieczenaApka\BezpiecznaAplikacja\PBIA_MVCAPP\PBIA_MVCAPP\Model1.edmx
-- --------------------------------------------------

SET QUOTED_IDENTIFIER OFF;
GO
USE [aspnet-PBIA_MVCAPP-20150530185522];
GO



IF OBJECT_ID(N'[dbo].[BannedIpAdresses]', 'U') IS NOT NULL
    DROP TABLE [dbo].[BannedIpAdresses];
GO


-- Creating table 'BannedIpAdresses'
CREATE TABLE [dbo].[BannedIpAdresses] (
    [ID] int IDENTITY(1,1) NOT NULL,
    [IPAddress] nvarchar(50)  NOT NULL
);
GO






-- Creating primary key on [ID] in table 'BannedIpAdresses'
ALTER TABLE [dbo].[BannedIpAdresses]
ADD CONSTRAINT [PK_BannedIpAdresses]
    PRIMARY KEY CLUSTERED ([ID] ASC);
GO


