--liquibase formatted sql
--changeset k1mb1:20260429-add-late-presence-type
ALTER TYPE presence_type ADD VALUE 'LATE';
