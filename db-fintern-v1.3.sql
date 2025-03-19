GO
/*
<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
<<<<<<<<<< BEGIN: CREATE DATABASE <<<<<<<<<<
<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
*/
	IF NOT EXISTS(SELECT * FROM sys.databases WHERE name = 'FPTInternship')
		CREATE DATABASE [FPTInternship];
/*
<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
<<<<<<<<<< END: CREATE DATABASE <<<<<<<<<<
<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
*/
GO
USE [FPTInternship];
GO
/*
<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
<<<<<<<<<< BEGIN: RESET DATABASE <<<<<<<<<<
<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
*/
	DECLARE @Sql NVARCHAR(500) DECLARE @Cursor CURSOR

	SET @Cursor = CURSOR FAST_FORWARD FOR
	SELECT DISTINCT sql = 'ALTER TABLE [' + tc2.TABLE_SCHEMA + '].[' +  tc2.TABLE_NAME + '] DROP [' + rc1.CONSTRAINT_NAME + '];'
	FROM INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS rc1
	LEFT JOIN INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc2 ON tc2.CONSTRAINT_NAME =rc1.CONSTRAINT_NAME

	OPEN @Cursor FETCH NEXT FROM @Cursor INTO @Sql

	WHILE (@@FETCH_STATUS = 0)
	BEGIN
	Exec sp_executesql @Sql
	FETCH NEXT FROM @Cursor INTO @Sql
	END

	CLOSE @Cursor DEALLOCATE @Cursor
	GO
	/* */
	DECLARE @tableName NVARCHAR(MAX)
	DECLARE tableCursor CURSOR FOR
	SELECT name
	FROM sys.tables

	OPEN tableCursor
	FETCH NEXT FROM tableCursor INTO @tableName

	WHILE @@FETCH_STATUS = 0
	BEGIN
		DECLARE @sql NVARCHAR(MAX)
		SET @sql = N'DROP TABLE ' + QUOTENAME(@tableName)
		EXEC sp_executesql @sql
		FETCH NEXT FROM tableCursor INTO @tableName
	END

	CLOSE tableCursor
	DEALLOCATE tableCursor
/*
>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
>>>>>>>>> END: RESET DATABASE >>>>>>>>>>
>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
*/

GO

/*
<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
<<<<<<<<<< BEGIN: TABLES <<<<<<<<<<
<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
*/

CREATE TABLE [Class] ( -- Class for each intern to join in
    [id] INT PRIMARY KEY IDENTITY(1,1),
    [class_name] NVARCHAR(255) NOT NULL,
	[number_of_interns] INT DEFAULT 0,
	[status] NVARCHAR(50) DEFAULT 'ENDED' CHECK ([status] IN ('NOT STARTED', 'ON GOING', 'ENDED')), 
	[manager_id] INT NOT NULL,
    [created_at] DATETIME NOT NULL DEFAULT GETDATE(),
    [updated_at] DATETIME DEFAULT NULL,
	[deleted_at] DATETIME DEFAULT NULL,
	[created_by] INT DEFAULT NULL,
    [updated_by] INT DEFAULT NULL,
    [deleted_by] INT DEFAULT NULL,
    [is_active] BIT DEFAULT 1
);

CREATE TABLE [user] ( -- include admin, employee, intern, guest 
    [id] INT PRIMARY KEY IDENTITY(1,1),
    [first_name] NVARCHAR(255),
    [last_name] NVARCHAR(255),
    [email] NVARCHAR(255) NOT NULL,
    [phone_number] NVARCHAR(255),
    [class_id] INT, -- For Intern Only
    [avatar_path] NVARCHAR(MAX) DEFAULT (N'/assets/img/user/default-avatar.png'),
	[gender] NVARCHAR(50) DEFAULT 'MALE' CHECK ([gender] IN ('MALE', 'FEMALE', 'OTHER')), 
    [role] NVARCHAR(50) DEFAULT 'GUEST' CHECK ([role] IN ('ADMIN', 'EMPLOYEE', 'INTERN', 'GUEST')), 
    [created_at] DATETIME NOT NULL DEFAULT GETDATE(),
    [updated_at] DATETIME DEFAULT NULL,
    [deleted_at] DATETIME DEFAULT NULL,
	[created_by] INT DEFAULT NULL,
    [updated_by] INT DEFAULT NULL,
    [deleted_by] INT DEFAULT NULL,
    [is_active] BIT DEFAULT 1,
);

CREATE TABLE [Attendance] ( -- check attendance for each class
	[attendance_id] INT PRIMARY KEY IDENTITY(1,1),
	[user_id] INT NOT NULL,
	[class_id] INT NOT NULL,
	[status] NVARCHAR(50) NOT NULL CHECK ([status] in ('PRESENT', 'ABSENT', 'LATE')),
	[check-out] DATETIME NOT NULL,
	[created_at] DATETIME NOT NULL DEFAULT GETDATE(), --CHECK-IN 
    [updated_at] DATETIME DEFAULT NULL, 
    [deleted_at] DATETIME DEFAULT NULL,
	[created_by] INT DEFAULT NULL,
    [updated_by] INT DEFAULT NULL,
    [deleted_by] INT DEFAULT NULL,
    [is_active] BIT DEFAULT 1,
	CONSTRAINT FK_Attendance_User FOREIGN KEY ([user_id]) REFERENCES [user]([id]),
	CONSTRAINT FK_Attendance_Class FOREIGN KEY ([class_id]) REFERENCES [Class]([id])
)

CREATE TABLE [Recruitment] (  -- Show the positions are needed in order to the guest can view and apply their CV
    [id] INT PRIMARY KEY IDENTITY(1,1),
    [position] NVARCHAR(255) NOT NULL,-- BA SE JAVA .NET KS NODE.JS AI ... DATA--
	[experience-requiment] NVARCHAR(255) NOT NULL, --JAVA .NET --
	[language] NVARCHAR(255), --english japanese--
	[min_GPA] FLOAT,
    [total_slot] INT NOT NULL,
	[description] NVARCHAR(MAX),
	[start_time] DATETIME NOT NULL,
	[end_time] DATETIME NOT NULL,
    [created_at] DATETIME NOT NULL DEFAULT GETDATE(),
    [updated_at] DATETIME DEFAULT NULL,
	[deleted_at] DATETIME DEFAULT NULL,
	[created_by] INT DEFAULT NULL,
    [updated_by] INT DEFAULT NULL,
    [deleted_by] INT DEFAULT NULL,
    [is_active] BIT DEFAULT 1
	CONSTRAINT FK_Requirement_CreatedBy FOREIGN KEY ([created_by]) REFERENCES [user]([id]) -- admin create
);
CREATE TABLE [File] ( -- Save the file include CV from intern or report from manager
    [id] INT PRIMARY KEY IDENTITY(1,1),
    [submitter_id] INT NOT NULL, --
    [display_Name] NVARCHAR(255) NOT NULL,-- Display name is the file name user submitted --
    [path] NVARCHAR(255) NOT NULL,
    [created_at] DATETIME NOT NULL DEFAULT GETDATE(),
    [updated_at] DATETIME DEFAULT NULL,
    [deleted_at] DATETIME DEFAULT NULL,
    [created_by] INT DEFAULT NULL,
    [updated_by] INT DEFAULT NULL,
    [deleted_by] INT DEFAULT NULL,
    [is_active] BIT DEFAULT 1,
    CONSTRAINT PK_CV_Intern FOREIGN KEY ([submitter_id]) REFERENCES [user]([id])
    );
CREATE TABLE [CV_Info] (
    [recruitment_id] INT NOT NULL,
    [file_id] INT NOT NULL,
	[gpa] FLOAT NOT NULL,
	[education] NVARCHAR(255) NOT NULL,
	[skill] NVARCHAR(255) NOT NULL,
    [created_at] DATETIME NOT NULL DEFAULT GETDATE(),
    [updated_at] DATETIME DEFAULT NULL,
    [deleted_at] DATETIME DEFAULT NULL,
    [created_by] INT DEFAULT NULL,
    [updated_by] INT DEFAULT NULL,
    [deleted_by] INT DEFAULT NULL,
    [is_active] BIT DEFAULT 1,
    CONSTRAINT FK_Recruitment_Id FOREIGN KEY ([recruitment_id]) REFERENCES [Recruitment]([id]),
    CONSTRAINT FK_File_Id FOREIGN KEY ([file_id]) REFERENCES [File]([id])
    )




CREATE TABLE [Conversation] ( -- room chat for each class
	[conversation_id] INT PRIMARY KEY IDENTITY(1,1),
	[conversation_name] NVARCHAR(255),
	[conversation_avatar] NVARCHAR(255),
	[created_at] DATETIME NOT NULL DEFAULT GETDATE(),
    [updated_at] DATETIME DEFAULT NULL,
    [deleted_at] DATETIME DEFAULT NULL,
	[created_by] INT DEFAULT NULL,
    [updated_by] INT DEFAULT NULL,
    [deleted_by] INT DEFAULT NULL,
    [is_active] BIT DEFAULT 1,
)

CREATE TABLE [Message] ( -- Show the history of message, who send, send date
	[message_id] INT PRIMARY KEY IDENTITY(1,1),
	[sender_id] INT,
	[conversation_id] INT,
	[message_content] NVARCHAR(MAX),
	[message_type] NVARCHAR(255),
	[send_date] DATETIME,
	[status] NVARCHAR(50) CHECK ([status] IN ('EDITED', 'DELETED', 'SENT')),
    [created_at] DATETIME NOT NULL DEFAULT GETDATE(),
    [updated_at] DATETIME DEFAULT NULL,
    [deleted_at] DATETIME DEFAULT NULL,
	[created_by] INT DEFAULT NULL,
    [updated_by] INT DEFAULT NULL,
    [deleted_by] INT DEFAULT NULL,
    [is_active] BIT DEFAULT 1,
	CONSTRAINT FK_Message_Sender FOREIGN KEY ([sender_id]) REFERENCES [user]([id]),
	CONSTRAINT FK_Message_Receiver1 FOREIGN KEY (conversation_id) REFERENCES [Conversation]([conversation_id]),
)

CREATE TABLE [Conversation_user] ( -- Show the member and admin of the conversation
	[user_id] INT,
	[conversation_id] INT,
	[isAdmin] BIT,
	[created_at] DATETIME NOT NULL DEFAULT GETDATE(),
    [updated_at] DATETIME DEFAULT NULL, 
    [deleted_at] DATETIME DEFAULT NULL,
	[created_by] INT DEFAULT NULL,
    [updated_by] INT DEFAULT NULL,
    [deleted_by] INT DEFAULT NULL,
    [is_active] BIT DEFAULT 1,
	CONSTRAINT FK_Conversation_User FOREIGN KEY ([user_id]) REFERENCES [user]([id]),
	CONSTRAINT FK_Conversation FOREIGN KEY ([conversation_id]) REFERENCES [Conversation]([conversation_id]),
	PRIMARY KEY([user_id], [conversation_id])
)

CREATE TABLE [Notification_entities] ( -- Show the entities of the notifications
	[id] INT PRIMARY KEY IDENTITY(1,1),
	[entity_name] NVARCHAR(255), -- Task Assigned, New Message, CV Submitted, Homework Submitted
	[created_at] DATETIME NOT NULL DEFAULT GETDATE(),
    [updated_at] DATETIME DEFAULT NULL,
    [deleted_at] DATETIME DEFAULT NULL,
	[created_by] INT DEFAULT NULL,
    [updated_by] INT DEFAULT NULL,
    [deleted_by] INT DEFAULT NULL,
    [is_active] BIT DEFAULT 1,
)

CREATE TABLE [Notification] ( -- Save the real notification user send in the system
    [id] INT PRIMARY KEY IDENTITY(1,1),
    [actor_id] INT NOT NULL, -- User who send notification
	[notification_type] INT NOT NULL, -- Enum notification type (task, message, cv, homework)
    [url] NVARCHAR(MAX) NOT NULL, -- path redirect to notification.
	[created_at] DATETIME NOT NULL DEFAULT GETDATE(),
    [updated_at] DATETIME DEFAULT NULL,
    [deleted_at] DATETIME DEFAULT NULL,
	[created_by] INT DEFAULT NULL,
    [updated_by] INT DEFAULT NULL,
    [deleted_by] INT DEFAULT NULL,
    [is_active] BIT DEFAULT 1,
    CONSTRAINT FK_Notification_User FOREIGN KEY ([actor_id]) REFERENCES [user]([id]),
	CONSTRAINT FK_Notification_Type FOREIGN KEY ([notification_type]) REFERENCES [Notification_entities]([id])
);


CREATE TABLE [Notification_visits] ( -- Confirm who received the noti
	[id] INT PRIMARY KEY IDENTITY(1,1),
	[notifier_id] INT, -- Receiver
	[notification_id] INT, -- notification description
	[created_at] DATETIME NOT NULL DEFAULT GETDATE(),
    [updated_at] DATETIME DEFAULT NULL,
    [deleted_at] DATETIME DEFAULT NULL,
	[created_by] INT DEFAULT NULL,
    [updated_by] INT DEFAULT NULL,
    [deleted_by] INT DEFAULT NULL,
    [is_active] BIT DEFAULT 1,
	CONSTRAINT FK_Notification_Notifier FOREIGN KEY ([notifier_id]) REFERENCES [user]([id]),
	CONSTRAINT FK_Notification_Id FOREIGN KEY ([notification_id]) REFERENCES [Notification]([id]),
)

CREATE TABLE [Notification_entities_types] ( -- Display the description of the entity
	[id] INT PRIMARY KEY IDENTITY(1,1),
	[type_name] NVARCHAR(255),
	[entity_id] INT,
	[description] NVARCHAR(255),
	[created_at] DATETIME NOT NULL DEFAULT GETDATE(),
    [updated_at] DATETIME DEFAULT NULL,
    [deleted_at] DATETIME DEFAULT NULL,
	[created_by] INT DEFAULT NULL,
    [updated_by] INT DEFAULT NULL,
    [deleted_by] INT DEFAULT NULL,
    [is_active] BIT DEFAULT 1,
	CONSTRAINT FK_Notification_Entity FOREIGN KEY ([entity_id]) REFERENCES [Notification_entities]([id]),
)

CREATE TABLE [Notification_room_settings] ( -- For some settings in chat room like mute noti or is owner of the room chat
	[id] INT PRIMARY KEY IDENTITY(1,1),
	[notifier_id] INT,
	[room_id] INT,
	[is_on] BIT DEFAULT 1, -- if user want to turn off the notification --> is_on == 0
	[entity_id] INT,
	[is_owner] BIT DEFAULT 1
	CONSTRAINT FK_Notification_Room_User FOREIGN KEY ([notifier_id]) REFERENCES [user]([id]),
	CONSTRAINT FK_Notification_Room FOREIGN KEY ([room_id]) REFERENCES [Conversation]([conversation_id]),
)

CREATE TABLE [Mentor] ( -- Mentor of each class
    [class_id] INT,
    [mentor_id] INT NOT NULL,    
	[created_at] DATETIME DEFAULT GETDATE(),
    [updated_at] DATETIME DEFAULT NULL,
    [deleted_at] DATETIME DEFAULT NULL,
    [updated_by] INT DEFAULT NULL,
    [deleted_by] INT DEFAULT NULL,
    [is_active] BIT DEFAULT 1,
    PRIMARY KEY ([class_id],[mentor_id]),
    CONSTRAINT FK_ClassManager_Class FOREIGN KEY ([class_id]) REFERENCES [Class]([id]) ON DELETE CASCADE,
    CONSTRAINT FK_ClassManager_Mentor FOREIGN KEY ([mentor_id]) REFERENCES [user]([id])
);


CREATE TABLE [Tasks] ( -- Assign task 
    [id] INT PRIMARY KEY IDENTITY(1,1),
    [class_id] INT NOT NULL,
    [task_name] NVARCHAR(255) NOT NULL,
    [start_time] DATETIME NOT NULL,
    [end_time] DATETIME NOT NULL,
	[description] NVARCHAR(MAX),
    [file] NVARCHAR(255),
    [created_at] DATETIME NOT NULL DEFAULT GETDATE(),
    [updated_at] DATETIME DEFAULT NULL,
    [deleted_at] DATETIME DEFAULT NULL,
	[created_by] INT NOT NULL,
    [updated_by] INT DEFAULT NULL,
    [deleted_by] INT DEFAULT NULL,
    [is_active] BIT DEFAULT 1,
    CONSTRAINT FK_Tasks_Class FOREIGN KEY ([class_id]) REFERENCES [Class]([id]),
	CONSTRAINT FK_Tasks_User FOREIGN KEY ([created_by]) REFERENCES [user]([id])
);


CREATE TABLE [CompletedTasks] (
	[task_id] INT NOT NULL,
	[user_id] INT NOT NULL,
	[class_id] INT NOT NULL,
	[file] NVARCHAR(255),
	[status] NVARCHAR(50) CHECK ([status] in ('PENDING', 'COMPLETED', 'REJECTED')),
	[comment] NVARCHAR(MAX),
	[created_at] DATETIME NOT NULL DEFAULT GETDATE(), -- Submission time
    [updated_at] DATETIME DEFAULT NULL,
	[deleted_at] DATETIME DEFAULT NULL,
	[created_by] INT DEFAULT NULL,
    [updated_by] INT DEFAULT NULL, -- submission's change time
    [deleted_by] INT DEFAULT NULL,
    [is_active] BIT DEFAULT 1,
	CONSTRAINT FK_CompletedTasks_User FOREIGN KEY ([user_id]) REFERENCES [user]([id]),
	CONSTRAINT FK_CompletedTasks_Class FOREIGN KEY ([class_id]) REFERENCES [Class]([id])
)

CREATE TABLE [Event] (
    [id] INT PRIMARY KEY IDENTITY(1,1),
    [date_of_event] DATETIME NOT NULL,
    [start_time] DATETIME NOT NULL,
    [end_time] DATETIME NOT NULL,
    [event_image] NVARCHAR(255),
    [organizer_id] INT NOT NULL,
    [created_at] DATETIME NOT NULL DEFAULT GETDATE(),
    [updated_at] DATETIME DEFAULT NULL,
    [deleted_at] DATETIME DEFAULT NULL,
    [updated_by] INT DEFAULT NULL,
    [deleted_by] INT DEFAULT NULL,
    [is_active] BIT DEFAULT 1,
    CONSTRAINT FK_Event_Organizer FOREIGN KEY ([organizer_id]) REFERENCES [user]([id])
);

CREATE TABLE [Feedback] ( 
    [id] INT PRIMARY KEY IDENTITY(1,1),
    [created_by] INT NOT NULL,
    [content] NVARCHAR(MAX) NOT NULL,
	[created_at] DATETIME NOT NULL DEFAULT GETDATE(),
    [updated_at] DATETIME DEFAULT NULL,
    [deleted_at] DATETIME DEFAULT NULL,
    [updated_by] INT DEFAULT NULL,
    [deleted_by] INT DEFAULT NULL,
    [is_active] BIT DEFAULT 1,
    CONSTRAINT FK_Feedback_Sender FOREIGN KEY ([created_by]) REFERENCES [User]([id]) 
);

ALTER TABLE [User]
ADD CONSTRAINT FK_User_Class FOREIGN KEY ([class_id]) REFERENCES [Class]([id]);

ALTER TABLE [Class]
ADD CONSTRAINT FK_Requirement_Manager FOREIGN KEY ([manager_id]) REFERENCES [user]([id]);

/*
>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
>>>>>>>>> END: TABLES >>>>>>>>>>
>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
*/
GO
/*
<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
<<<<<<<<<< BEGIN:TRIGGER <<<<<<<<<<
<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
*/

/*increase number of intern when add intern into class */
CREATE TRIGGER UpdateNumberOfInterns 
ON [user]
AFTER INSERT
AS 
BEGIN 
	UPDATE [Class]
	SET number_of_interns = number_of_interns + 1
	FROM [Class]
	INNER JOIN INSERTED intern ON [Class].id = intern.class_id

END;
GO

/* decrease number of slot when a guest apply cv into recruitment */
CREATE TRIGGER UpdateTotalSlots
ON [Recruitment]
AFTER INSERT
AS 
BEGIN
	UPDATE [Recruitment]
	SET available_slot = available_slot - 1
	FROM [Recruitment]
	WHERE id IN (SELECT id FROM INSERTED)
END;


/*
>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
>>>>>>>>> END: TRIGGERS >>>>>>>>>>
>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
*/
GO
/*
<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
<<<<<<<<<< BEGIN: EXAMPLE DATA <<<<<<<<<<
<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
*/

INSERT INTO [user] ([first_name], [last_name], [email], [phone_number], [class_id], [gender], [role]) 
VALUES 
('FPT', 'SOFTWARE', 'FPT@FPT.EDU.VN.com', '1234567890', NULL, 'MALE', 'ADMIN'),
('Jane', 'Smith', 'jane.smith@example.com', '0987654321', NULL, 'FEMALE', 'EMPLOYEE');

-- Class
INSERT INTO [Class] ([class_name], [number_of_interns] , [manager_id], [created_by]) 
VALUES 
('Class A', 0, 1, 1),
('Class B', 15, 2, 1),
('JS01', 20, 2, 1),
('Class C', 30, 2, 1);

-- User
INSERT INTO [user] ([first_name], [last_name], [email], [phone_number], [class_id], [gender], [role]) 
VALUES
('Alice', 'Johnson', 'alice.johnson@example.com', '1122334455', 1, 'MALE', 'INTERN'),
('Bob', 'Williams', 'bob.williams@example.com', '5566778899', NULL, 'FEMALE', 'GUEST');

-- Recruitment
INSERT INTO [Recruitment] ([position], [salary], [experience], [education], [work_form], [total_slot], [available_slot], [start_time], [end_time], [created_by]) 
VALUES 
('Software Engineer', 1000, '2 years', 'Bachelor', 'FULL-TIME', 5, 3, GETDATE(), DATEADD(DAY, 30, GETDATE()), 1),
('Project Manager', 2000, '5 years', 'Master', 'PART-TIME', 2, 1, GETDATE(), DATEADD(DAY, 15, GETDATE()), 1);


INSERT INTO [File] ([submitter_id], [file_type], [displayName], [path]) 
VALUES 
(3, 'CV', 'alice_cv.pdf', '/files/alice_cv.pdf'),
(4, 'REPORT', 'bob_report.docx', '/files/bob_report.docx');

-- Conversation
INSERT INTO [Conversation] ([conversation_name], [conversation_avatar]) 
VALUES 
('Project A Discussion', '/images/project_a.png'),
('Team Meeting', '/images/team_meeting.png');

-- Message
INSERT INTO [Message] ([sender_id], [conversation_id], [message_content], [message_type], [send_date], [status]) 
VALUES 
(1, 1, 'Hello team!', 'TEXT', GETDATE(), 'SENT'),
(2, 1, 'Hi! Lets discuss the project.', 'TEXT', GETDATE(), 'SENT');

-- Notification_entities
INSERT INTO [Notification_entities] ([entity_name]) 
VALUES 
('Task Assigned'),
('New Message'),
('CV Submitted');

-- Notification
INSERT INTO [Notification] ([actor_id], [notification_type], [url]) 
VALUES 
(1, 1, '/tasks/1'),
(2, 2, '/messages/1');

-- Tasks
INSERT INTO [Tasks] ([class_id], [task_name], [start_time], [end_time], [file], [created_by]) 
VALUES 
(1, 'Homework 1', GETDATE(), DATEADD(DAY, 7, GETDATE()), '/tasks/homework1.pdf', 1),
(2, 'Final Project', GETDATE(), DATEADD(DAY, 30, GETDATE()), '/tasks/final_project.zip', 2);

-- Event
INSERT INTO [Event] ([date_of_event], [start_time], [end_time], [event_image], [organizer_id]) 
VALUES 
(GETDATE(), GETDATE(), DATEADD(HOUR, 2, GETDATE()), '/images/event1.jpg', 1),
(GETDATE(), GETDATE(), DATEADD(HOUR, 3, GETDATE()), '/images/event2.jpg', 2);

--Feedback
INSERT INTO [Feedback] ([created_by], [content]) 
VALUES 
(3, 'This internship program is very helpful!'),
(4, 'Need more guidance on projects.');

--  Mentor
INSERT INTO [Mentor] ([class_id], [mentor_id]) 
VALUES 
(1, 1),
(2, 2);

-- Notification_visits
INSERT INTO [Notification_visits] ([notifier_id], [notification_id]) 
VALUES 
(3, 1),
(4, 2);


