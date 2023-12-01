// Source/Wrestle/WRGameMode.cpp
#include "WRGameMode.h"
#include "BoxWrestlerCharacter.h"
#include "WRPlayerState.h"
#include "WRGameState.h"

AWRGameMode::AWRGameMode()
{
    DefaultPawnClass = ABoxWrestlerCharacter::StaticClass();
    PlayerStateClass = AWRPlayerState::StaticClass();
    GameStateClass   = AWRGameState::StaticClass();
}
