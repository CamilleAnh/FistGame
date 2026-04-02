using System.Collections;
using UnityEngine;

public class ClickableMover : MonoBehaviour
{
    [Header("Grid & Movement")]
    public float gridSize = 1f;
    public float moveDuration = 0.2f;

    [Header("Collision")]
    public LayerMask obstacleLayer;
    public float checkRadius = 0.1f;

    bool isMoving;
    Camera mainCam;

    void Awake()
    {
        mainCam = Camera.main;
    }

    void Update()
    {
        if (Input.GetMouseButtonDown(0))
        {
            Ray ray = mainCam.ScreenPointToRay(Input.mousePosition);
            RaycastHit2D hit = Physics2D.GetRayIntersection(ray, Mathf.Infinity);
            if (hit.collider != null && hit.collider.gameObject == gameObject)
            {
                TryMoveToAdjacent();
            }
        }

        if (Input.touchCount > 0)
        {
            Touch t = Input.GetTouch(0);
            if (t.phase == TouchPhase.Began)
            {
                Ray ray = mainCam.ScreenPointToRay(t.position);
                RaycastHit2D hit = Physics2D.GetRayIntersection(ray, Mathf.Infinity);
                if (hit.collider != null && hit.collider.gameObject == gameObject)
                {
                    TryMoveToAdjacent();
                }
            }
        }
    }

    void TryMoveToAdjacent()
    {
        if (isMoving) return;

        Vector2[] dirs = new Vector2[] { Vector2.up, Vector2.right, Vector2.down, Vector2.left };
        foreach (var d in dirs)
        {
            Vector2 target = (Vector2)transform.position + d * gridSize;
            if (IsPositionFree(target))
            {
                StartCoroutine(MoveTo(target));
                return;
            }
        }
    }

    bool IsPositionFree(Vector2 pos)
    {
        return Physics2D.OverlapCircle(pos, checkRadius, obstacleLayer) == null;
    }

    IEnumerator MoveTo(Vector2 target)
    {
        isMoving = true;
        Vector3 start = transform.position;
        Vector3 end = new Vector3(target.x, target.y, start.z);
        if (moveDuration <= 0f)
        {
            transform.position = end;
            isMoving = false;
            yield break;
        }
        float elapsed = 0f;
        while (elapsed < moveDuration)
        {
            transform.position = Vector3.Lerp(start, end, elapsed / moveDuration);
            elapsed += Time.deltaTime;
            yield return null;
        }
        transform.position = end;
        isMoving = false;
    }
}
