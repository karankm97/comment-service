import requests
import json
import time

base_url = "http://api.commentservice.com:8080/v1/"
headers = {
    'Content-Type': 'application/json'
}
generic_post_payload = {
    "body": "Test Comment",
    "user": "John Doe",
    "parentId": "0"
}

init_comments = []
with open('test-resources/comment_post.json') as f:
    init_comments = json.load(f)
    
print(init_comments)

init_reactions = []
with open('test-resources/reaction_post.json') as f:
    init_reactions = json.load(f)

print(init_reactions)

parent_ids = [0]
comment_ids = []

def test_post_new_comments():
    print("test_post_new_comments")
    url = base_url + "comment/"
    for payload in init_comments:
        response = requests.request("POST", url, headers=headers, data=json.dumps(payload))
        assert response.status_code == 200
        data = response.json()
        parent_ids.append(data["parentId"])
        comment_ids.append(data["id"])
        print("Posted comment successfully with id: " + str(data["id"]))
        time.sleep(1)

    print("State of parent id list: " + str(parent_ids))
    print("State of comment id list: " + str(comment_ids))
    print("test_post_comment has passed")


def test_update_comment():
    print("test_update_comment")
    url = base_url + "comment/"
    update_payload = {
        "body": "Test Comment 1 edited",
        "user": "John Doe",
        "commentId": "1"
    }
    response = requests.request("PUT", url, headers=headers, data=json.dumps(update_payload))
    assert response.status_code == 200
    data = response.json()
    assert data["body"] == update_payload["body"]

    get_response = requests.request("GET", url + update_payload["commentId"], headers=headers)
    assert get_response.status_code == 200
    data = get_response.json()
    assert data["body"] == update_payload["body"]
    print("test_update_comment has passed")

def test_delete_comment():
    print("test_delete_comment")
    url = base_url + "comment/"
    response = requests.request("DELETE", url + "16?user=Virat Kohli", headers=headers)
    assert response.status_code == 200

    get_response = requests.request("GET", url + "16", headers=headers)
    assert get_response.status_code == 200
    data = get_response.json()
    assert data["body"] == "Deleted by user"
    assert data["isDeleted"] == True
    print("test_delete_comment has passed")

def test_get_next_level_comments():
    print("test_get_next_level_comments")
    print("Checking default response")
    url = base_url + "comment/0/nextlevel"
    response = requests.request("GET", url, headers=headers)
    assert response.status_code == 200
    data = response.json()
    assert data["size"] == 10
    assert data["pageNo"] == 0
    assert data["pageSize"] == 10

    print("Checking paginated response 1")
    response = requests.request("GET", url + "?pageNo=1&pageSize=5", headers=headers)
    assert response.status_code == 200
    data = response.json()
    assert data["size"] == 5
    assert data["pageNo"] == 1
    assert data["pageSize"] == 5

    print("Checking paginated response 2")
    response = requests.request("GET", url + "?pageNo=2&pageSize=5", headers=headers)
    assert response.status_code == 200
    data = response.json()
    assert data["size"] == 1
    assert data["pageNo"] == 2
    assert data["pageSize"] == 5
    print("test_get_next_level_comment has passed")

def test_get_full_tree_comments():
    print("test_get_full_tree_comments")
    print("Checking default response")
    url = base_url + "comment/0/fulltree"
    response = requests.request("GET", url, headers=headers)
    assert response.status_code == 200
    data = response.json()
    assert len(data["comments"]) == 11
    assert data["maxDepth"] == 5
    print("test_get_full_tree_comments has passed")


def test_post_new_reactions():
    print("test_post_new_reactions")
    url = base_url + "comment/reaction/"
    for payload in init_reactions:
        response = requests.request("POST", url, headers=headers, data=json.dumps(payload))
        assert response.status_code == 200
        data = response.json()
        print("Posted reaction successfully with type: " + str(data["reactionType"]))
        time.sleep(1)
    
    print("test_post_new_reactions has passed")

def test_get_users_for_reaction():
    print("test_get_users_for_reaction")
    print("test_get_users_for_reaction default reply")
    url = base_url + "comment/1/reaction/"
    response = requests.request("GET", url + "LIKE/users", headers=headers)
    assert response.status_code == 200
    data = response.json()
    assert data["pageNo"] == 0
    assert data["pageSize"] == 10
    assert data["size"] == 5
    assert len(data["users"]) == 5

    print("test_get_users_for_reaction paginated reply 1")
    response = requests.request("GET", url + "LIKE/users?pageNo=1&pageSize=2", headers=headers)
    assert response.status_code == 200
    data = response.json()
    assert data["pageNo"] == 1
    assert data["pageSize"] == 2
    assert data["size"] == 2
    assert len(data["users"]) == 2

    print("test_get_users_for_reaction paginated reply 2")
    response = requests.request("GET", url + "LIKE/users?pageNo=2&pageSize=2", headers=headers)
    assert response.status_code == 200
    data = response.json()
    assert data["pageNo"] == 2
    assert data["pageSize"] == 2
    assert data["size"] == 1
    assert len(data["users"]) == 1

    print("test_get_users_for_reaction dislike reply")
    url = base_url + "comment/1/reaction/"
    response = requests.request("GET", url + "DISLIKE/users?pageNo=1&pageSize=2", headers=headers)
    assert response.status_code == 200
    data = response.json()
    assert data["pageNo"] == 1
    assert data["pageSize"] == 2
    assert data["size"] == 1
    assert len(data["users"]) == 1
    print("test_get_users_for_reaction has passed")

def test_update_reaction_for_user():
    print("test_update_reaction_for_user")
    url = base_url + "comment/reaction/"
    payload = {
        "commentId": 1,
        "user": "Babar Azam",
        "reactionType": "LIKE"
    }
    response = requests.request("PATCH", url, headers=headers, data=json.dumps(payload))
    assert response.status_code == 200
    data = response.json()
    print("Updated reaction successfully with type: " + str(data["reactionType"]))

    url = base_url + "comment/1/reaction/"
    response = requests.request("GET", url + "DISLIKE/users?pageNo=1&pageSize=2", headers=headers)
    assert response.status_code == 200
    data = response.json()
    assert data["pageNo"] == 1
    assert data["pageSize"] == 2
    assert data["size"] == 0
    assert len(data["users"]) == 0
    print("test_update_reaction_for_user has passed")


def test_delete_reaction_for_user():
    print("test_delete_reaction_for_user")
    url = base_url + "comment/1/reaction?user=Rick G"

    response = requests.request("DELETE", url, headers=headers)
    assert response.status_code == 200
    print("Deleted reaction successfully")

    url = base_url + "comment/1/reaction/"
    response = requests.request("GET", url + "DISLIKE/users", headers=headers)
    assert response.status_code == 200
    data = response.json()
    assert data["pageNo"] == 0
    assert data["pageSize"] == 10
    assert data["size"] == 1
    assert len(data["users"]) == 1
    print("test_delete_reaction_for_user has passed")

def test_verify_final_state():
    print("verify_final_state")
    print("Checking full tree response")
    url = base_url + "comment/0/fulltree"
    response = requests.request("GET", url, headers=headers)
    assert response.status_code == 200
    data = response.json()
    assert len(data["comments"]) == 11
    assert data["maxDepth"] == 5
    assert data["comments"][0]["comment"]["likeCount"] == 6
    assert data["comments"][0]["comment"]["dislikeCount"] == 1

    print("Checking next level response")
    url = base_url + "comment/0/nextlevel"
    response = requests.request("GET", url, headers=headers)
    assert response.status_code == 200
    data = response.json()
    assert data["size"] == 10
    assert data["pageNo"] == 0
    assert data["pageSize"] == 10
    assert data["comments"][0]["comment"]["likeCount"] == 6
    assert data["comments"][0]["comment"]["dislikeCount"] == 1

    print("Checking next level response when user clicks view more")
    url = base_url + "comment/1/nextlevel"
    response = requests.request("GET", url, headers=headers)
    assert response.status_code == 200
    data = response.json()
    assert data["size"] == 4
    assert data["pageNo"] == 0
    assert data["pageSize"] == 10
    assert data["comments"][0]["comment"]["likeCount"] == 0
    assert data["comments"][0]["comment"]["dislikeCount"] == 0

    print("Checking next level response when user clicks view more")
    url = base_url + "comment/12/nextlevel"
    response = requests.request("GET", url, headers=headers)
    assert response.status_code == 200
    data = response.json()
    assert data["size"] == 1
    assert data["pageNo"] == 0
    assert data["pageSize"] == 10
    assert data["comments"][0]["comment"]["likeCount"] == 0
    assert data["comments"][0]["comment"]["dislikeCount"] == 0
    print("verify_final_state has passed")


if __name__ == '__main__':
    start = time.time()
    test_post_new_comments()
    test_update_comment()
    test_delete_comment()
    test_get_next_level_comments()
    test_get_full_tree_comments()
    test_post_new_reactions()
    test_get_users_for_reaction()
    test_update_reaction_for_user()
    test_delete_reaction_for_user()
    test_verify_final_state()
    end = time.time()
    print("API Automation Suite took " + str(end - start) + " to run.")