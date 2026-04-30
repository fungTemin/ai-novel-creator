-- V2: 修复 JSONB 列类型为 TEXT（匹配 JPA String 类型）

ALTER TABLE characters ALTER COLUMN relationships TYPE TEXT USING relationships::text;
ALTER TABLE world_building ALTER COLUMN details TYPE TEXT USING details::text;
