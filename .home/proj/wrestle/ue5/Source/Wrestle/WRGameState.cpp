// Source/Wrestle/WRGameState.cpp
#include "WRGameState.h"
#include "Net/UnrealNetwork.h"
#include "WRPlayerState.h"

AWRGameState::AWRGameState()
{
    bReplicates = true;
    GlobalContestId = 1;
}

void AWRGameState::AwardPoint(AWRPlayerState* Scorer)
{
    if (!HasAuthority() || !Scorer) return;
    Scorer->Score += 1.f;                 // built-in float Score
    Scores.FindOrAdd(Scorer) += 1;        // replicated map for quick UI
    // (optionally: multicast a toast or sound here)
}

void AWRGameState::GetLifetimeReplicatedProps(TArray<FLifetimeProperty>& Out) const
{
    Super::GetLifetimeReplicatedProps(Out);
    DOREPLIFETIME(AWRGameState, GlobalContestId);
    DOREPLIFETIME(AWRGameState, Scores);
}
