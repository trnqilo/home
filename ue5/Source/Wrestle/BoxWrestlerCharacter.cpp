// Source/Wrestle/BoxWrestlerCharacter.cpp
#include "BoxWrestlerCharacter.h"
#include "WRGameState.h"
#include "WRPlayerState.h"
#include "Components/CapsuleComponent.h"
#include "Components/StaticMeshComponent.h"
#include "GameFramework/CharacterMovementComponent.h"
#include "Engine/StaticMesh.h"
#include "UObject/ConstructorHelpers.h"
#include "Net/UnrealNetwork.h"

ABoxWrestlerCharacter::ABoxWrestlerCharacter()
{
    bReplicates = true;
    GetCharacterMovement()->MaxWalkSpeed = 600.f;
    MoveSpeed = 1.f;

    // Visual cube attached to the capsule so we look like a box
    BoxVisual = CreateDefaultSubobject<UStaticMeshComponent>(TEXT("BoxVisual"));
    BoxVisual->SetupAttachment(GetCapsuleComponent());
    static ConstructorHelpers::FObjectFinder<UStaticMesh> CubeMesh(TEXT("/Engine/BasicShapes/Cube.Cube"));
    if (CubeMesh.Succeeded())
    {
        BoxVisual->SetStaticMesh(CubeMesh.Object);
        BoxVisual->SetRelativeScale3D(FVector(0.8f, 0.8f, 0.8f)); // fit the capsule
        BoxVisual->SetCollisionEnabled(ECollisionEnabled::NoCollision);
    }

    // Overlap events on our capsule to detect "wrestle contact"
    UCapsuleComponent* Cap = GetCapsuleComponent();
    Cap->SetCollisionEnabled(ECollisionEnabled::QueryAndPhysics);
    Cap->SetCollisionResponseToAllChannels(ECR_Block);
    Cap->SetGenerateOverlapEvents(true);
    Cap->OnComponentBeginOverlap.AddDynamic(this, &ABoxWrestlerCharacter::OnBeginOverlap);
    Cap->OnComponentEndOverlap.AddDynamic(this, &ABoxWrestlerCharacter::OnEndOverlap);

    CurrentContestId = 0;
    bContestOpen = false;
}

void ABoxWrestlerCharacter::BeginPlay()
{
    Super::BeginPlay();
}

void ABoxWrestlerCharacter::SetupPlayerInputComponent(UInputComponent* IC)
{
    Super::SetupPlayerInputComponent(IC);
    IC->BindAxis("MoveForward", this, &ABoxWrestlerCharacter::MoveForward);
    IC->BindAxis("MoveRight",   this, &ABoxWrestlerCharacter::MoveRight);
    IC->BindAction("ClaimContact", IE_Pressed, this, &ABoxWrestlerCharacter::ClaimContactPressed);
}

void ABoxWrestlerCharacter::MoveForward(float V)
{
    if (Controller && V != 0.f)
        AddMovementInput(FVector::ForwardVector, V * MoveSpeed);
}

void ABoxWrestlerCharacter::MoveRight(float V)
{
    if (Controller && V != 0.f)
        AddMovementInput(FVector::RightVector, V * MoveSpeed);
}

AWRGameState* ABoxWrestlerCharacter::GetWRGameState() const
{
    return GetWorld() ? GetWorld()->GetGameState<AWRGameState>() : nullptr;
}

void ABoxWrestlerCharacter::OnBeginOverlap(UPrimitiveComponent*, AActor* Other, UPrimitiveComponent*, int32, bool, const FHitResult&)
{
    if (!HasAuthority()) return;

    auto* OtherChar = Cast<ABoxWrestlerCharacter>(Other);
    if (!OtherChar || OtherChar == this) return;

    // open contest only if neither is already contesting someone else
    if (!bContestOpen && !OtherChar->bContestOpen)
    {
        if (AWRGameState* GS = GetWRGameState())
        {
            GS->GlobalContestId++;
            CurrentContestId = GS->GlobalContestId;
            OtherChar->CurrentContestId = GS->GlobalContestId;

            CurrentOpponent = OtherChar;
            OtherChar->CurrentOpponent = this;

            bContestOpen = true;
            OtherChar->bContestOpen = true;
        }
    }
}

void ABoxWrestlerCharacter::OnEndOverlap(UPrimitiveComponent*, AActor* Other, UPrimitiveComponent*, int32)
{
    if (!HasAuthority()) return;

    if (Other == CurrentOpponent.Get())
    {
        // close/cancel the contest
        bContestOpen = false;
        CurrentOpponent = nullptr;
        CurrentContestId = 0;
    }
    if (auto* OtherChar = Cast<ABoxWrestlerCharacter>(Other))
    {
        OtherChar->bContestOpen = false;
        OtherChar->CurrentOpponent = nullptr;
        OtherChar->CurrentContestId = 0;
    }
}

void ABoxWrestlerCharacter::ClaimContactPressed()
{
    // clients always ask the server with the contest they think is active
    ServerClaimContact(CurrentContestId);
}

void ABoxWrestlerCharacter::ServerClaimContact_Implementation(int32 ContestId)
{
    if (!HasAuthority()) return;

    // validate: must be in an open contest and ids match
    auto* MePS = GetPlayerState<AWRPlayerState>();
    auto* Opp  = CurrentOpponent.Get();
    auto* GS   = GetWRGameState();

    const bool Valid = bContestOpen && Opp && Opp->bContestOpen &&
                       ContestId != 0 && ContestId == CurrentContestId &&
                       ContestId == Opp->CurrentContestId;

    if (Valid && GS && MePS)
    {
        // award and close the contest for both
        GS->AwardPoint(MePS);
        bContestOpen = false;
        CurrentContestId = 0;
        if (Opp)
        {
            Opp->bContestOpen = false;
            Opp->CurrentContestId = 0;
            Opp->CurrentOpponent = nullptr;
        }
        CurrentOpponent = nullptr;
    }
}

void ABoxWrestlerCharacter::GetLifetimeReplicatedProps(TArray<FLifetimeProperty>& Out) const
{
    Super::GetLifetimeReplicatedProps(Out);
    DOREPLIFETIME(ABoxWrestlerCharacter, CurrentOpponent);
    DOREPLIFETIME(ABoxWrestlerCharacter, CurrentContestId);
    DOREPLIFETIME(ABoxWrestlerCharacter, bContestOpen);
}
