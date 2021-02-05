package models

import "time"

type AccessDetails struct {
	UserEmail string
	UserID    int64
	CreatedAt time.Time
}
