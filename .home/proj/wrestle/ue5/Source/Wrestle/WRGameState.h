// Source/Wrestle/WRGameState.h
#pragma once
#include "GameFramework/GameStateBase.h"
#include "WRGameState.generated.h"

UCLASS()
class WRESTLEBOXES_API AWRGameState : public AGameStateBase
{
    GENERATED_BODY()
public:
    AWRGameState();

    // monotonically increasing id for each contact contest
    UPROPERTY(Replicated, VisibleAnywhere, BlueprintReadOnly)
    int32 GlobalContestId;

    // simple leaderboard (also available on each PlayerState)
    UPROPERTY(Replicated, VisibleAnywhere, BlueprintReadOnly)
    TMap<APlayerState*, int32> Scores;

    void AwardPoint(class AWRPlayerState* Scorer);

    virtual void GetLifetimeReplicatedProps(TArray<FLifetimeProperty>& OutLifetimeProps) const override;
};
