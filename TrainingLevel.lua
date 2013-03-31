
WIDTH = 15
HEIGHT = 80
GRAVITY_Y = -30
PLAYER_SPAWN_X = 2
PLAYER_SPAWN_Y = 2

--fixtureDef.isSensor = false
--fixtureDef.friction = .2
print(levelBody)
print(box2DFactory)
print(fixtureDef)
fixtureDef.isSensor = false
--print(levelBody)
box2DFactory:createEdge(levelBody, fixtureDef, 0, 0, WIDTH, 0)
box2DFactory:createEdge(levelBody, fixtureDef, 0, 0, 0, HEIGHT)
box2DFactory:createEdge(levelBody, fixtureDef, WIDTH, 0, WIDTH, HEIGHT)

box2DFactory:createEdge(levelBody, fixtureDef, 0, 1, 5, 1)
box2DFactory:createEdge(levelBody, fixtureDef, 7, 2, 9, 2)
box2DFactory:createEdge(levelBody, fixtureDef, 10, 8f, 11, 8f)
box2DFactory:createEdge(levelBody, fixtureDef, 10, 1, 11, 1)

ladderX = WIDTH - 2
for i = 1, 10, 1 do
    box2DFactory:createEdge(levelBody, fixtureDef, ladderX, i, WIDTH, i)
end

box2DFactory:createEdge(levelBody, fixtureDef, 15, 0f, 20, 2f)

bodyDef.position:set(2,2)