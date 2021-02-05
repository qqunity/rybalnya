package models

import (
	"fmt"
	"github.com/golang/protobuf/ptypes/timestamp"
)

type CaughtCreature struct {
	Id              int64 `json:id`
	CreatureId      int64 `json:creatureId`
	CreaturePointer *AquaticCreature
	UserId          int64 `json:userId`
	UserPointer     *User
	CaughtAt        timestamp.Timestamp `json:caughtAt`
	Lat             float64             `json:latitude`
	Long            float64             `json:longitude`
	CreatureWeight  float32             `json:creatureWeight`
}

func (c *CaughtCreature) String() string {
	return fmt.Sprintf(
		"User<%d \t %d \t %d \t %f \t %f \t %f>",
		c.Id,
		c.CreatureId,
		c.UserId,
		c.Lat,
		c.Long,
		c.CreatureWeight,
	)
}
