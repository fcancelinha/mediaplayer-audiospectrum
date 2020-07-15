-- TABLES

drop database PLAYLIST

create database PLAYLIST
use PLAYLIST


create table ALBUM(
	ID INT IDENTITY PRIMARY KEY,
	ALBUM NVARCHAR(100) UNIQUE NOT NULL,
	ANO nvarchar(50) NULL
)

create table ARTISTA(
	ID INT IDENTITY PRIMARY KEY,
	ARTISTA NVARCHAR(100) UNIQUE NOT NULL
)

create table ESTILO(
	ID INT IDENTITY PRIMARY KEY,
	ESTILO NVARCHAR(100) UNIQUE NOT NULL
)

create table MUSICA(
	ID INT IDENTITY PRIMARY KEY,
	NOME NVARCHAR(200) NOT NULL,
	LOCALIZACAO NVARCHAR(500) UNIQUE NOT NULL,
	ID_ALBUM INT REFERENCES ALBUM(ID) NULL,
	ID_ARTISTA INT REFERENCES ARTISTA(ID) NULL,
	ID_ESTILO INT REFERENCES ESTILO(ID) NULL
)

create table UTILIZADOR(
	ID INT IDENTITY PRIMARY KEY,
	UTILIZADOR NVARCHAR(50) UNIQUE NOT NULL,
	PASSWORD NVARCHAR(75) NOT NULL,
	SECRET NVARCHAR(75) NOT NULL
)

CREATE TABLE PLAYLISTS(
	ID int identity primary key,
	NOME NVARCHAR(100) NOT NULL,
	ID_UTILIZADOR INT REFERENCES UTILIZADOR(ID)
)

CREATE TABLE PLAYMUSIC(
	ID_MUSICA INT REFERENCES MUSICA(ID),
	ID_PLAYLIST INT REFERENCES PLAYLISTS(ID) ON DELETE CASCADE,
	ID_PLAYLISTUTILIZADOR INT REFERENCES UTILIZADOR(ID),
	primary key(ID_PLAYLIST, ID_MUSICA, ID_PLAYLISTUTILIZADOR)
)




--UTILIZADOR

-- CHECK LOGIN



GO
create or alter proc checkLogin(@username nvarchar(50), @password nvarchar(75), @output nvarchar(200) output) AS
BEGIN TRY
		
		
		IF NOT EXISTS (select '*' from UTILIZADOR where UTILIZADOR.UTILIZADOR = @username)
			THROW 60002, 'Username is not registered', 10

		IF NOT EXISTS(select '*' from UTILIZADOR where UTILIZADOR.UTILIZADOR = @username AND UTILIZADOR.PASSWORD = @password)
			THROW 60001, 'Incorrect username or password', 10
		
	
			select UTILIZADOR.ID, UTILIZADOR.UTILIZADOR
			from UTILIZADOR
			where UTILIZADOR.UTILIZADOR = @username

END TRY
BEGIN CATCH
	set @output = ERROR_MESSAGE()
END CATCH


-- REGISTER USER

GO
create or alter proc registerUser(@username nvarchar(50), @password nvarchar(75), @secret nvarchar(75), @output nvarchar(200) output) AS
BEGIN TRY
BEGIN TRAN
		IF EXISTS(select '*' from UTILIZADOR where UTILIZADOR.UTILIZADOR = @username)
			THROW 60001, 'This username already exists', 10
		ELSE
			insert into UTILIZADOR values(UPPER(@username), @password, @secret)
COMMIT
END TRY
BEGIN CATCH
	set @output = ERROR_MESSAGE();
	ROLLBACK
END CATCH


-- RECOVER PASSWORD

GO
create or alter proc recoverPassword(@username nvarchar(50), @secret nvarchar(75), @output nvarchar(200) output) AS
BEGIN TRY
	IF NOT EXISTS(select '*' from UTILIZADOR where UTILIZADOR.UTILIZADOR = @username)
		THROW 60001, 'Username does not exist',10
	
	IF NOT EXISTS(select '*' from UTILIZADOR where UTILIZADOR.UTILIZADOR = @username and UTILIZADOR.SECRET = @secret)
		THROW 60002, 'Wrong secret for this username', 10

	set @output = (select PASSWORD from UTILIZADOR where UTILIZADOR.UTILIZADOR = @username)

END TRY
BEGIN CATCH
	set @output = ERROR_MESSAGE()
END CATCH


--ALTER PASSWORD 

GO
create or alter proc alterPassword(@username nvarchar(50), @oldPassword nvarchar(75),  @newPassword nvarchar(75), @output nvarchar(200) output)AS
BEGIN TRY
BEGIN TRAN

	IF NOT EXISTS(SELECT '*' FROM UTILIZADOR where UTILIZADOR.PASSWORD = @oldPassword AND UTILIZADOR.UTILIZADOR = @username)
		THROW 60001, 'Current password is incorrect',10

	IF @oldPassword = @newPassword
		THROW 60002, 'New password cannot be the same as the old one', 10

	UPDATE UTILIZADOR
	SET UTILIZADOR.PASSWORD = @newPassword
	WHERE UTILIZADOR.UTILIZADOR = @username

COMMIT
END TRY
BEGIN CATCH
	set @output = ERROR_MESSAGE()
	ROLLBACK
END CATCH


-- DELETE USER

GO
CREATE OR ALTER PROC deleteUser(@username nvarchar(50)) AS
BEGIN TRY
BEGIN TRAN

	DELETE FROM UTILIZADOR
	WHERE UTILIZADOR.UTILIZADOR = @username

COMMIT
END TRY
BEGIN CATCH
	print ERROR_MESSAGE();
	ROLLBACK
END CATCH

-- TRIGGERS

GO
CREATE OR ALTER TRIGGER t_onDeleteUser ON UTILIZADOR INSTEAD OF DELETE AS
BEGIN TRY
BEGIN TRAN

	IF EXISTS (SELECT '*' FROM PLAYLISTS INNER JOIN DELETED ON DELETED.ID = PLAYLISTS.ID_UTILIZADOR)
		delete from PLAYLISTS where PLAYLISTS.ID_UTILIZADOR IN (SELECT deleted.ID FROM deleted iNNER JOIN PLAYLISTS ON DELETED.ID = PLAYLISTS.ID_UTILIZADOR)

	delete from UTILIZADOR where UTILIZADOR.ID IN (SELECT deleted.ID from deleted)

COMMIT
END TRY
BEGIN CATCH
	ROLLBACK
END CATCH
	


--PLAYLIST

-- INSERÇÃO DE PLAYLISTS

GO
create or alter proc usp_addPlaylist(@nome nvarchar(100), @idUtilizador int, @output nvarchar(300) output) AS
BEGIN TRY
BEGIN TRAN
	
		if exists (select '*' from PLAYLISTS where PLAYLISTS.NOME = @nome and PLAYLISTS.ID_UTILIZADOR = @idUtilizador)
			THROW 60001, 'This Playlist already exists', 10

		insert into PLAYLISTS values(@nome, @idUtilizador)
		set @output = 'Playlist successfully added'
COMMIT
END TRY
BEGIN CATCH
	set @output = ERROR_MESSAGE();
	ROLLBACK
END CATCH


-- BUSCAR PLAYLISTS

GO
create or alter proc usp_getPlaylist(@idUtilizador int) AS 
select PLAYLISTS.NOME
from PLAYLISTS
where PLAYLISTS.ID_UTILIZADOR =  @idUtilizador


-- CARREGAR MUSICA / ALBUM / ARTISTA / ESTILO

-- ALBUM

GO
CREATE OR ALTER PROC usp_AddAlbum(@album nvarchar(100), @ano int) AS
BEGIN TRY
BEGIN TRAN
	
	IF EXISTS(select '*' from ALBUM where ALBUM.ALBUM = @album)
	BEGIN	
		IF NOT EXISTS(select '*' from ALBUM where ALBUM.ANO = @album)
			update ALBUM set ALBUM.ANO = @ano where ALBUM.ALBUM = @album
	END
	ELSE 
		insert into ALBUM values(@album, IIF(@ano IS NOT NULL, @ano, null))
COMMIT
END TRY
BEGIN CATCH
	ROLLBACK;
END CATCH


-- ARTISTA

GO
CREATE OR ALTER PROC usp_saveArtist(@artist nvarchar(100)) AS 
BEGIN TRY
BEGIN TRAN
	IF NOT EXISTS(select '*' from ARTISTA where ARTISTA.ARTISTA = @artist)
		insert into ARTISTA values(@artist)
COMMIT
END TRY
BEGIN CATCH
	ROLLBACK;
END CATCH


-- ESTILO

GO
CREATE OR ALTER PROC usp_AddEstilo(@estilo nvarchar(100)) AS
BEGIN TRY
BEGIN TRAN
	IF NOT EXISTS(select '*' from ESTILO where ESTILO.ESTILO = @estilo)
	insert into ESTILO values(@estilo)
COMMIT
END TRY
BEGIN CATCH
	ROLLBACK;
END CATCH


-- MUSICA

GO
CREATE OR ALTER PROC usp_saveMusic(@nome nvarchar(200), 
								   @localizacao nvarchar(500),
								   @album nvarchar(100), 
								   @artista nvarchar(100), 
								   @estilo nvarchar(100),
								   @playlistID nvarchar(100),
								   @IDutilizador int,
								   @output nvarchar(500) output)
AS

DECLARE @IDALBUM INT, @IDARTISTA INT, @IDESTILO INT

BEGIN TRY

		
		--CHAVES ESTRANGEIRAS
		SELECT @IDALBUM = ALBUM.ID from ALBUM where ALBUM.ALBUM = @album
		SELECT @IDARTISTA = ARTISTA.ID from ARTISTA where ARTISTA.ARTISTA = @artista
		SELECT @IDESTILO = ESTILO.ID from ESTILO where ESTILO.ESTILO = @estilo
	
BEGIN TRAN
		--INSERÇÃO OU UPDATE DA MÚSICA
	IF NOT EXISTS (SELECT '*' FROM MUSICA WHERE MUSICA.NOME = @nome AND MUSICA.LOCALIZACAO = @localizacao)
		insert into MUSICA values(@nome, @localizacao, @IDALBUM, @IDARTISTA, @IDESTILO)
	ELSE
		BEGIN
			update MUSICA
			set MUSICA.ID_ALBUM = IIF(@album IS NULL, MUSICA.ID_ALBUM, @IDALBUM),
				MUSICA.ID_ARTISTA = IIF(@artista IS NULL, MUSICA.ID_ARTISTA, @IDARTISTA),
				MUSICA.ID_ESTILO = IIF(@estilo IS NULL, MUSICA.ID_ESTILO, @IDESTILO)
		  where MUSICA.NOME = @nome and MUSICA.LOCALIZACAO = @localizacao
		END

	--LIGAÇÃO DA MÚSICA À TABELA INTERMÉDIA -- ADICIONAR SAFEGUARD

		INSERT INTO PLAYMUSIC VALUES((select MUSICA.ID from MUSICA where MUSICA.NOME = @nome),
									 (select PLAYLISTS.ID from PLAYLISTS where PLAYLISTS.NOME = @playlistID AND PLAYLISTS.ID_UTILIZADOR = @IDutilizador),
									 @IDutilizador)

COMMIT
END TRY
BEGIN CATCH
	set @output = ERROR_MESSAGE();
	ROLLBACK;
END CATCH


-- Recuperar Musicas

GO
CREATE OR ALTER PROC usp_retrieveMusics(@playlistID nvarchar(100), @IDuser int) AS
BEGIN TRY

	select MUSICA.nome as 'Nome', localizacao as 'Path', COALESCE(ALBUM, 'N/A') as 'Album', COALESCE(ARTISTA, 'N/A') as 'Artista', COALESCE(ESTILO, 'N/A') as 'Estilo', COALESCE(ALBUM.ANO, 'N/A') AS 'ANO', MUSICA.ID as 'ID'
	from MUSICA left join ALBUM on ALBUM.ID = MUSICA.ID_ALBUM
				left join ARTISTA on ARTISTA.ID = MUSICA.ID_ARTISTA
				left join ESTILO on ESTILO.ID = MUSICA.ID_ESTILO
				left join PLAYMUSIC on MUSICA.ID = PLAYMUSIC.ID_MUSICA
				left join PLAYLISTS on PLAYLISTS.ID = PLAYMUSIC.ID_PLAYLIST
	where PLAYMUSIC.ID_PLAYLISTUTILIZADOR = @IDuser and PLAYLISTS.NOME = @playlistID

END TRY
BEGIN CATCH
END CATCH

select * from PLAYLISTS
select * from MUSICA
select * from PLAYMUSIC
select * from UTILIZADOR


-- REMOVER MÚSICA 

GO
CREATE OR ALTER PROC usp_DeleteMusic(@playlistID nvarchar(100), @musicaID int, @IDuser int) AS
BEGIN TRY
BEGIN TRAN
		
		delete from PLAYMUSIC where PLAYMUSIC.ID_MUSICA = @musicaID AND PLAYMUSIC.ID_PLAYLIST = (select PLAYLISTS.ID from PLAYLISTS where PLAYLISTS.NOME = @playlistID AND 
																																		  PLAYLISTS.ID_UTILIZADOR = @IDuser)
COMMIT
END TRY
BEGIN CATCH
	print ERROR_MESSAGE()
	ROLLBACK;
END CATCH

select * from PLAYMUSIC


-- REMOVER PLAYLIST	

GO
CREATE OR ALTER PROC usp_DeletePlaylist(@playlistID nvarchar(100), @IDuser int) AS
BEGIN TRY
BEGIN TRAN
	
		delete from PLAYLISTS where PLAYLISTS.NOME = @playlistID AND PLAYLISTS.ID_UTILIZADOR = @IDuser

COMMIT
END TRY
BEGIN CATCH
	ROLLBACK;
END CATCH
