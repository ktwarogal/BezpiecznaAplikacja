
-- --------------------------------------------------
-- Entity Designer DDL Script for SQL Server 2005, 2008, and Azure
-- --------------------------------------------------
-- Date Created: 06/30/2015 23:25:21
-- Generated from EDMX file: F:\Repozytorium\BezpieczenaApka\BezpiecznaAplikacja\PBIA_MVCAPP\PBIA_MVCAPP\Model1.edmx
-- --------------------------------------------------

SET QUOTED_IDENTIFIER OFF;
GO
USE [aspnet-PBIA_MVCAPP-20150530185522];
GO
IF SCHEMA_ID(N'dbo') IS NULL EXECUTE(N'CREATE SCHEMA [dbo]');
GO

-- --------------------------------------------------
-- Dropping existing FOREIGN KEY constraints
-- --------------------------------------------------

IF OBJECT_ID(N'[dbo].[FK_webpages_UsersInRoles_webpages_Roles]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[webpages_UsersInRoles] DROP CONSTRAINT [FK_webpages_UsersInRoles_webpages_Roles];
GO
IF OBJECT_ID(N'[dbo].[FK_webpages_UsersInRoles_UserProfile]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[webpages_UsersInRoles] DROP CONSTRAINT [FK_webpages_UsersInRoles_UserProfile];
GO

-- --------------------------------------------------
-- Dropping existing tables
-- --------------------------------------------------

IF OBJECT_ID(N'[dbo].[UserProfile]', 'U') IS NOT NULL
    DROP TABLE [dbo].[UserProfile];
GO
IF OBJECT_ID(N'[dbo].[webpages_Membership]', 'U') IS NOT NULL
    DROP TABLE [dbo].[webpages_Membership];
GO
IF OBJECT_ID(N'[dbo].[webpages_OAuthMembership]', 'U') IS NOT NULL
    DROP TABLE [dbo].[webpages_OAuthMembership];
GO
IF OBJECT_ID(N'[dbo].[webpages_Roles]', 'U') IS NOT NULL
    DROP TABLE [dbo].[webpages_Roles];
GO
IF OBJECT_ID(N'[dbo].[BannedIpAdresses]', 'U') IS NOT NULL
    DROP TABLE [dbo].[BannedIpAdresses];
GO
IF OBJECT_ID(N'[dbo].[webpages_UsersInRoles]', 'U') IS NOT NULL
    DROP TABLE [dbo].[webpages_UsersInRoles];
GO

-- --------------------------------------------------
-- Creating all tables
-- --------------------------------------------------

-- Creating table 'UserProfile'
CREATE TABLE [dbo].[UserProfile] (
    [UserId] int IDENTITY(1,1) NOT NULL,
    [UserName] nvarchar(max)  NULL
);
GO

-- Creating table 'webpages_Membership'
CREATE TABLE [dbo].[webpages_Membership] (
    [UserId] int  NOT NULL,
    [CreateDate] datetime  NULL,
    [ConfirmationToken] nvarchar(128)  NULL,
    [IsConfirmed] bit  NULL,
    [LastPasswordFailureDate] datetime  NULL,
    [PasswordFailuresSinceLastSuccess] int  NOT NULL,
    [Password] nvarchar(128)  NOT NULL,
    [PasswordChangedDate] datetime  NULL,
    [PasswordSalt] nvarchar(128)  NOT NULL,
    [PasswordVerificationToken] nvarchar(128)  NULL,
    [PasswordVerificationTokenExpirationDate] datetime  NULL
);
GO

-- Creating table 'webpages_OAuthMembership'
CREATE TABLE [dbo].[webpages_OAuthMembership] (
    [Provider] nvarchar(30)  NOT NULL,
    [ProviderUserId] nvarchar(100)  NOT NULL,
    [UserId] int  NOT NULL
);
GO

-- Creating table 'webpages_Roles'
CREATE TABLE [dbo].[webpages_Roles] (
    [RoleId] int IDENTITY(1,1) NOT NULL,
    [RoleName] nvarchar(256)  NOT NULL
);
GO

-- Creating table 'BannedIpAdresses'
CREATE TABLE [dbo].[BannedIpAdresses] (
    [ID] int IDENTITY(1,1) NOT NULL,
    [IPAddress] nvarchar(50)  NOT NULL,
    [BannedDate] datetime  NOT NULL
);
GO




-- Creating primary key on [ID] in table 'BannedIpAdresses'
ALTER TABLE [dbo].[BannedIpAdresses]
ADD CONSTRAINT [PK_BannedIpAdresses]
    PRIMARY KEY CLUSTERED ([ID] ASC);
GO



-- --------------------------------------------------
-- Script has ended
-- --------------------------------------------------