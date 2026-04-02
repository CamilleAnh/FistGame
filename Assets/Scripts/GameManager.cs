using System;
using System.Collections;
using UnityEngine;
using UnityEngine.SceneManagement;

public class GameManager : MonoBehaviour
{
    public static GameManager Instance { get; private set; }

    public enum GameState { MainMenu, Playing, Paused, GameOver }
    public GameState CurrentState { get; private set; }

    public event Action<GameState> OnStateChanged;

    [Header("UI References (optional)")]
    public GameObject mainMenuUI;
    public GameObject pauseUI;
    public GameObject gameOverUI;
    public GameObject levelCompleteUI;

    [Header("Level load settings")]
    public float nextLevelDelay = 2f;
    private bool isTransitioning = false;

    private void Awake()
    {
        if (Instance != null && Instance != this)
        {
            Destroy(gameObject);
            return;
        }
        Instance = this;
        DontDestroyOnLoad(gameObject);
    }

    private void Start()
    {
        SetState(GameState.MainMenu);
    }

    public void SetState(GameState newState)
    {
        if (CurrentState == newState) return;
        CurrentState = newState;

        switch (newState)
        {
            case GameState.MainMenu:
                Time.timeScale = 0f;
                break;
            case GameState.Playing:
                Time.timeScale = 1f;
                break;
            case GameState.Paused:
                Time.timeScale = 0f;
                break;
            case GameState.GameOver:
                Time.timeScale = 0f;
                break;
        }

        if (mainMenuUI) mainMenuUI.SetActive(newState == GameState.MainMenu);
        if (pauseUI) pauseUI.SetActive(newState == GameState.Paused);
        if (gameOverUI) gameOverUI.SetActive(newState == GameState.GameOver);
        if (levelCompleteUI) levelCompleteUI.SetActive(false);

        OnStateChanged?.Invoke(newState);
    }

    public void StartGame()
    {
        SetState(GameState.Playing);
    }

    public void PauseGame()
    {
        if (CurrentState != GameState.Playing) return;
        SetState(GameState.Paused);
    }

    public void ResumeGame()
    {
        if (CurrentState != GameState.Paused) return;
        SetState(GameState.Playing);
    }

    public void TriggerGameOver()
    {
        SetState(GameState.GameOver);
        if (gameOverUI) gameOverUI.SetActive(true);
    }

    public void LevelComplete(bool autoLoadNext = true, float delay = -1f)
    {
        if (isTransitioning) return;
        isTransitioning = true;

        SetState(GameState.Paused);
        if (levelCompleteUI) levelCompleteUI.SetActive(true);

        if (autoLoadNext)
        {
            if (delay < 0f) delay = nextLevelDelay;
            StartCoroutine(LoadNextLevelAfterDelay(delay));
        }
        else
        {
            isTransitioning = false;
        }
    }

    private IEnumerator LoadNextLevelAfterDelay(float delay)
    {
        yield return new WaitForSecondsRealtime(delay);

        int next = SceneManager.GetActiveScene().buildIndex + 1;
        if (next < SceneManager.sceneCountInBuildSettings)
        {
            Time.timeScale = 1f;
            SceneManager.LoadScene(next);
            SetState(GameState.Playing);
        }
        else
        {
            Time.timeScale = 1f;
            SceneManager.LoadScene(0);
            SetState(GameState.MainMenu);
        }

        isTransitioning = false;
    }

    public void LoadScene(int buildIndex)
    {
        Time.timeScale = 1f;
        SceneManager.LoadScene(buildIndex);
        SetState(GameState.Playing);
    }
}
