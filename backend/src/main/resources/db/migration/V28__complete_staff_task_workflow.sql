ALTER TABLE staff_tasks
  ADD COLUMN updated_at timestamptz,
  ADD COLUMN started_at timestamptz;

UPDATE staff_tasks SET updated_at = COALESCE(completed_at, created_at)
WHERE updated_at IS NULL;

ALTER TABLE staff_tasks ALTER COLUMN updated_at SET NOT NULL;

CREATE TABLE staff_task_updates (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  task_id uuid NOT NULL REFERENCES staff_tasks(id) ON DELETE CASCADE,
  actor_user_id uuid NOT NULL REFERENCES users(id),
  event_type varchar(30) NOT NULL,
  from_status varchar(20),
  to_status varchar(20),
  note text,
  created_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_staff_task_updates_task
ON staff_task_updates(task_id, created_at);

CREATE INDEX idx_staff_tasks_assignee_due
ON staff_tasks(society_id, assigned_to, status, due_at);
