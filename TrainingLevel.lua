local WIDTH = 30
local HEIGHT = 80
local GRAVITY_Y = -30
local PLAYER_SPAWN_X = 4
local PLAYER_SPAWN_Y = 4
local JUMP_DELTA_Y = 1.3
local KICKOFF_DELTA_Y = 3.5

fixtureDef.isSensor = false
box2DFactory:createEdge(levelBody, fixtureDef, 0, 0, WIDTH, 0)
box2DFactory:createEdge(levelBody, fixtureDef, 0, 0, 0, HEIGHT)
box2DFactory:createEdge(levelBody, fixtureDef, WIDTH, 0, WIDTH, HEIGHT)

box2DFactory:createEdge(levelBody, fixtureDef, 0, JUMP_DELTA_Y, 5, JUMP_DELTA_Y)
box2DFactory:createEdge(levelBody, fixtureDef, 7, JUMP_DELTA_Y*2, 9, JUMP_DELTA_Y*2)
box2DFactory:createEdge(levelBody, fixtureDef, 10, JUMP_DELTA_Y*2 + KICKOFF_DELTA_Y, 11, JUMP_DELTA_Y*2 + KICKOFF_DELTA_Y)
box2DFactory:createEdge(levelBody, fixtureDef, 0, JUMP_DELTA_Y*2 + KICKOFF_DELTA_Y*2, 5, JUMP_DELTA_Y*2 + KICKOFF_DELTA_Y*2)
box2DFactory:createEdge(levelBody, fixtureDef, 10, JUMP_DELTA_Y, 11, JUMP_DELTA_Y)

local ladderX = WIDTH - 2
for i = 0, 10, 1 do
    local platformHeight = JUMP_DELTA_Y+i*(KICKOFF_DELTA_Y-.3)
    box2DFactory:createEdge(levelBody, fixtureDef, ladderX, platformHeight, WIDTH, platformHeight)
end

fixtureDef.friction=0
box2DFactory:createEdge(levelBody, fixtureDef, 15, 0f, 20, 2f)

world:setGravity(world:getGravity():set(0,GRAVITY_Y))
bodyDef.position:set(PLAYER_SPAWN_X,PLAYER_SPAWN_Y)