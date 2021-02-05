package models

import "fmt"

type AquaticCreature struct {
	Id    int64
	IsSea bool
	Name  string
}

func (a *AquaticCreature) String() string {
	return fmt.Sprintf("AquaticCreature<%d \t %t \t %s>", a.Id, a.IsSea, a.Name)
}
