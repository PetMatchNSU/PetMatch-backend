DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_database WHERE datname = 'petmatch') THEN
       CREATE DATABASE petmatch;
    END IF;
END $$;