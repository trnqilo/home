// Source/Wrestle/BoxWrestlerCharacter.h
#pragma once
#include "GameFramework/Character.h"
#include "BoxWrestlerCharacter.generated.h"

class UStaticMeshComponent;
class UInputAction;
class UInputMappingContext;
class UCapsuleComponent;
class AWRGameState;
class AWRPlayerState;

UCLASS()
class WRESTLEBOXES_API ABoxWrestlerCharacter : public ACharacter
{
    GENERATED_BODY()
public:
    ABoxWrestlerCharacter();

protected:
    virtual void BeginPlay() override;
    virtual void SetupPlayerInputComponent(class UInputComponent* PlayerInputComponent) override;
    virtual void GetLifetimeReplicatedProps(TArray<FLifetimeProperty>& OutLifetimeProps) const override;

    UFUNCTION()
    void OnBeginOverlap(UPrimitiveComponent* OverlappedComp, AActor* OtherActor,
                        UPrimitiveComponent* OtherComp, int32 OtherBodyIndex, bool bFromSweep, const FHitResult& Hit);

    UFUNCTION()
    void OnEndOverlap(UPrimitiveComponent* OverlappedComp, AActor* OtherActor,
                      UPrimitiveComponent* OtherComp, int32 OtherBodyIndex);

    // local input
    void MoveForward(float V);
    void MoveRight(float V);
    void ClaimContactPressed();

    // server RPC to claim the current contest
    UFUNCTION(Server, Reliable)
    void ServerClaimContact(int32 ContestId);

    // helper
    AWRGameState* GetWRGameState() const;

protected:
    // replicated who we’re currently overlapping/contesting with
    UPROPERTY(Replicated, VisibleAnywhere, BlueprintReadOnly)
    TWeakObjectPtr<ABoxWrestlerCharacter> CurrentOpponent;

    // replicated id that participants must echo back when claiming
    UPROPERTY(Replicated, VisibleAnywhere, BlueprintReadOnly)
    int32 CurrentContestId;

    // simple guard to avoid double-awards for the same contest
    UPROPERTY(Replicated, VisibleAnywhere, BlueprintReadOnly)
    bool bContestOpen;

    // movement speed tweak
    UPROPERTY(EditAnywhere, BlueprintReadWrite, Category="Wrestle")
    float MoveSpeed;

    // a visible cube on the capsule
    UPROPERTY(VisibleAnywhere)
    UStaticMeshComponent* BoxVisual;
};
